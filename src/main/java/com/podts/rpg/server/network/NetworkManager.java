package com.podts.rpg.server.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import com.podts.rpg.server.Server;
import com.podts.rpg.server.Server.ServerStatus;

public abstract class NetworkManager {
	
	public enum NetworkStatus {
		OFFLINE(false, false),
		BINDING(false, false),
		BOUND(true, true),
		ONLINE(true, true),
		UNINITIALIZING(true, false),
		UNBINDING(false, false);
		
		private final boolean isBound;
		private final boolean canUnbind;
		
		public boolean isBound() {
			return isBound;
		}
		
		public boolean canUnBind() {
			return canUnbind;
		}
		
		private NetworkStatus(boolean isBound, boolean canUnbind) {
			this.isBound = isBound;
			this.canUnbind = canUnbind;
		}
		
	}
	
	private NetworkStatus status;
	private int port;
	private final Set<BiConsumer<NetworkStatus,NetworkStatus>> statusHooks;
	
	private final BiConsumer<ServerStatus,ServerStatus> serverOnlineHook = new BiConsumer<ServerStatus,ServerStatus>() {
		@Override
		public void accept(ServerStatus oldStatus, ServerStatus newStatus) {
			if(newStatus != ServerStatus.ONLINE) {
				Server.get().removeStatusHook(this);
				return;
			}
			//TODO Process all current connections credentials.
			changeStatus(NetworkStatus.ONLINE);
		}
	};
	
	protected void changeStatus(final NetworkStatus newStatus) {
		if(status == newStatus) return;
		synchronized(this) {
			final NetworkStatus oldStatus = status;
			status = newStatus;
			
			if(newStatus == NetworkStatus.OFFLINE) port = -1;
			
			notifyAll();
			for(BiConsumer<NetworkStatus,NetworkStatus> hook : statusHooks) {
				hook.accept(oldStatus,newStatus);
			}
		}
	}
	
	public int getPort() {
		return port;
	}
	
	public final boolean isBound() {
		return status.isBound();
	}
	
	public final boolean bind(int port) {
		return bind("localhost", port);
	}
	
	public final boolean bind(String address, int port) {
		changeStatus(NetworkStatus.BINDING);
		final boolean boundSuccessful = doBind(address, port);
		
		if(boundSuccessful) {
			this.port = port;
			Server.get().addStatusHook(serverOnlineHook);
			changeStatus(NetworkStatus.BOUND);
		} else {
			changeStatus(NetworkStatus.OFFLINE);
		}
		
		return boundSuccessful;
	}
	
	protected abstract boolean doBind(String address, int port);
	
	public final boolean unbind() {
		if(status.canUnBind()) return false;
		
		changeStatus(NetworkStatus.UNINITIALIZING);
		//TODO close all connections
		
		changeStatus(NetworkStatus.UNBINDING);
		doUnbind();
		changeStatus(NetworkStatus.OFFLINE);
		
		return true;
	}
	
	protected abstract void doUnbind();
	
	public abstract Collection<? extends Stream> getStreams();
	
	protected final void onPlayerDisconnect(Stream stream) {
		System.out.println("Client forcible closed connection from " + stream.getAddress());
	}
	
	public NetworkManager() {
		status = NetworkStatus.OFFLINE;
		statusHooks = new HashSet<BiConsumer<NetworkStatus,NetworkStatus>>();
	}
	
}
