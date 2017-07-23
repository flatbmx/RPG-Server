package com.podts.rpg.server;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import com.podts.rpg.server.model.Galaxy;
import com.podts.rpg.server.network.NetworkManager;
import com.podts.rpg.server.network.netty.NettyNetworkManager;

public final class Server {
	
	private static Server instance;
	
	public static Server get() {
		return instance;
	}
	
	public enum ServerStatus {
		OFFLINE(),
		LOADING(),
		UNLOADING(),
		ONLINE();
	}
	
	private ServerStatus status;
	private final Set<BiConsumer<ServerStatus,ServerStatus>> statusHooks;
	private int networkListenPort;
	
	private final NetworkManager networkManager;
	
	private Thread shutdownHook = new Thread() {
		@Override
		public void run() {
			Server.this.stop();
		}
	};
	
	public ServerStatus getStatus() {
		return status;
	}
	
	public boolean isRunning() {
		return status == ServerStatus.ONLINE;
	}
	
	public boolean addStatusHook(BiConsumer<ServerStatus,ServerStatus> hook) {
		if(hook == null) throw new IllegalArgumentException("Cannot add a null Shutdown Hook.");
		return statusHooks.add(hook);
	}
	
	public boolean removeStatusHook(BiConsumer<ServerStatus,ServerStatus> hook) {
		if(hook == null) throw new IllegalArgumentException("Cannot remove a null Shutdown Hook.");
		return statusHooks.remove(hook);
	}
	
	private void changeStatus(final ServerStatus newStatus) {
		if(status == newStatus) return;
		synchronized(this) {
			final ServerStatus oldStatus = status;
			status = newStatus;
			
			notifyAll();
			for(BiConsumer<ServerStatus,ServerStatus> hook : statusHooks) {
				hook.accept(oldStatus, newStatus);
			}
		}
	}
	
	public void start() {
		if(status != ServerStatus.OFFLINE) return;
		changeStatus(ServerStatus.LOADING);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		
		if (!networkManager.bind(networkListenPort)) {
			System.out.println("Server failed to bind to port " + networkManager.getPort());
			changeStatus(ServerStatus.OFFLINE);
			return;
		}
		
		System.out.println("Server bound to port " + networkManager.getPort());
		
		Galaxy.get();
		
		changeStatus(ServerStatus.ONLINE);
		
	}
	
	public void stop() {
		if(status != ServerStatus.ONLINE) return;
		changeStatus(ServerStatus.UNLOADING);
		System.out.println("Stopping Server.");
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
		networkManager.unbind();
		changeStatus(ServerStatus.OFFLINE);
	}
	
	private Server(int port) {
		if(instance == null) instance = this;
		status = ServerStatus.OFFLINE;
		networkListenPort = port;
		statusHooks = new HashSet<BiConsumer<ServerStatus,ServerStatus>>();
		networkManager = new NettyNetworkManager();
	}
	
	public static void main(String[] args) {
		Server server = new Server(1999);
		server.start();
	}
	
}
