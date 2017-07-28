package com.podts.rpg.server;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.network.NetworkManager;
import com.podts.rpg.server.network.netty.NettyNetworkManager;

public final class Server {
	
	/**
	 * Stores the instance of the server.
	 */
	private static Server instance;
	
	/**
	 * Get the current instance of the server running in this JVM.
	 * @return The current server instance.
	 */
	public static Server get() {
		return instance;
	}
	
	/**
	 * Represents the current status of the server.
	 * @author David
	 *
	 */
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
	
	/**
	 * Starts the server, the server can only be started in the <code>ServerStatus.OFFLINE</code> phase.
	 */
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
		
		Universe.get();
		
		changeStatus(ServerStatus.ONLINE);
		
	}
	
	/**
	 * Stops the server. This can only be ran when the server is in <code>ServerStatus.ONLINE</code> phase.
	 */
	public void stop() {
		if(status != ServerStatus.ONLINE) return;
		changeStatus(ServerStatus.UNLOADING);
		System.out.println("Stopping Server.");
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
		networkManager.unbind();
		changeStatus(ServerStatus.OFFLINE);
	}
	
	/**
	 * Creates a new instance of a server that will listen on the specified port.
	 * @param port
	 */
	private Server(int port) {
		if(instance == null) instance = this;
		status = ServerStatus.OFFLINE;
		networkListenPort = port;
		statusHooks = new HashSet<>();
		networkManager = new NettyNetworkManager();
	}
	
	public static void main(String[] args) {
		Server server = new Server(1999);
		server.start();
	}
	
}
