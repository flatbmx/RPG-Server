package com.podts.rpg.server.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.podts.rpg.server.Server;
import com.podts.rpg.server.Server.ServerStatus;
import com.podts.rpg.server.network.packet.LoginPacket;

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
	
	protected static NetworkManager networkManager;
	
	private NetworkStatus status;
	private int port;
	private final Set<NetworkStatusHook> statusHooks = new HashSet<NetworkStatusHook>();;
	private final LinkedList<StreamListener> streamListeners = new LinkedList<>();
	private final StreamListener veryLastStreamListener;
	
	private final List<LoginPacket> loginRequests = new LinkedList<>();
	
	public final boolean addStreamListenerLast(StreamListener listener) {
		if(streamListeners.contains(listener)) return false;
		streamListeners.addLast(listener);
		return true;
	}
	
	public final boolean addStreamListenerFirst(StreamListener listener) {
		if(streamListeners.contains(listener)) return false;
		streamListeners.addFirst(listener);
		return true;
	}
	
	public final boolean removeStreamListener(StreamListener listener) {
		return streamListeners.remove(listener);
	}
	
	protected final void addLoginRequest(LoginPacket p) {
		loginRequests.add(p);
	}
	
	protected final void handleLoginRequests() {
		for(LoginPacket p : loginRequests) {
			PacketHandler.handlePacket(p);
		}
		loginRequests.clear();
	}
	
	protected final void setPacketStream(Packet packet, NetworkStream networkStream) {
		packet.setStream(networkStream);
	}
	
	private final Server.ServerStatusHook serverOnlineHook = new Server.ServerStatusHook() {
		@Override
		public void accept(ServerStatus oldStatus, ServerStatus newStatus) {
			Server.get().removeStatusHook(this);
			if(newStatus != ServerStatus.ONLINE) {
				return;
			}
			changeStatus(NetworkStatus.ONLINE);
			handleLoginRequests();
		}
	};
	
	public final NetworkStatus getStatus() {
		return status;
	}
	
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
		return getStatus().isBound();
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
			networkManager = this;
			changeStatus(NetworkStatus.BOUND);
		} else {
			changeStatus(NetworkStatus.OFFLINE);
		}
		
		return boundSuccessful;
	}
	
	protected abstract boolean doBind(String address, int port);
	
	public final boolean unbind() {
		if(!status.canUnBind()) return false;
		
		changeStatus(NetworkStatus.UNINITIALIZING);
		//TODO close all connections
		
		changeStatus(NetworkStatus.UNBINDING);
		doUnbind();
		changeStatus(NetworkStatus.OFFLINE);
		
		return true;
	}
	
	protected abstract void doUnbind();
	
	public abstract Collection<? extends NetworkStream> getStreams();
	
	public Stream<? extends NetworkStream> streams() {
		return getStreams().stream();
	}
	
	protected final void onPlayerDisconnect(NetworkStream networkStream) {
		for(StreamListener listener : streamListeners) {
			listener.onDisconnect(networkStream);
		}
		if(veryLastStreamListener != null) veryLastStreamListener.onDisconnect(networkStream);
	}
	
	public NetworkManager(StreamListener last) {
		status = NetworkStatus.OFFLINE;
		veryLastStreamListener = last;
	}
	
	public NetworkManager() {
		this(null);
	}
	
}
