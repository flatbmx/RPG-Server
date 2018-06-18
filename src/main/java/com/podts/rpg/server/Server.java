package com.podts.rpg.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import com.podts.rpg.server.account.AcceptingAccountLoader;
import com.podts.rpg.server.command.CommandHandler;
import com.podts.rpg.server.model.PlayerLoginListener;
import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.model.universe.Universe.WorldAlreadyExistsException;
import com.podts.rpg.server.model.universe.generators.PerlinNoiseGenerator;
import com.podts.rpg.server.network.NetworkManager;
import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.StreamListener;
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
	 *
	 */
	public enum ServerStatus {
		OFFLINE(),
		LOADING(),
		UNLOADING(),
		ONLINE();
	}
	
	private final Logger logger;
	
	private ServerStatus status;
	private final Set<BiConsumer<ServerStatus,ServerStatus>> statusHooks;
	private final Set<PlayerLoginListener> playerLoginListeners = new HashSet<>();
	private int networkListenPort;
	
	private final NetworkManager networkManager;
	private final AccountLoader accountLoader;
	private final CommandHandler commandHandler;
	
	private final Player[] players;
	private final Map<String,Player> playerNameMap = new HashMap<>();
	private final int MAX_PLAYERS = 100;
	
	private int currentPlayerID = 0;
	private Queue<Integer> recycledPlayerIDs = new LinkedList<>();
	
	private int getNewID() {
		if(!recycledPlayerIDs.isEmpty()) return recycledPlayerIDs.poll();
		return currentPlayerID++;
	}
	
	private final Thread shutdownHook = new Thread() {
		@Override
		public void run() {
			Server.this.stop();
		}
	};
	
	public ServerStatus getStatus() {
		return status;
	}
	
	public final Logger getLogger() {
		return logger;
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
	
	public boolean addPlayerLoginListener(PlayerLoginListener listener) {
		if(listener == null) throw new IllegalArgumentException("Cannot add a null PlayerLoginListener.");
		return playerLoginListeners.add(listener);
	}
	
	public boolean removePlayerLoginListener(PlayerLoginListener listener) {
		if(listener == null) throw new IllegalArgumentException("Cannot remove a null PlayerLoginListener.");
		return playerLoginListeners.remove(listener);
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
	
	public final AccountLoader getAccountLoader() {
		return accountLoader;
	}
	
	public final CommandHandler getCommandHandler() {
		return commandHandler;
	}
	
	public Player getPlayer(int id) {
		if(id >= 0 && id < MAX_PLAYERS) return players[id];
		return null;
	}
	
	Player createPlayer(String username, String password) {
		return new Player(getNewID(), username, password);
	}
	
	public void logoutPlayer(Player player) {
		player.getEntity().deRegister();
		for(PlayerLoginListener listener : playerLoginListeners) {
			listener.onPlayerLogout(player);
		}
		int id = player.getID();
		players[id] = null;
		playerNameMap.remove(player.getUsername());
		recycledPlayerIDs.add(id);
		System.out.println(player.getUsername() + " logged out.");
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
		
		GameEngine.create(4);
		
		try {
			Universe.get().createWorld("Earth", new PerlinNoiseGenerator());
		} catch (WorldAlreadyExistsException e) {
			e.printStackTrace();
		}
		
		changeStatus(ServerStatus.ONLINE);
		System.out.println("Server is now online and can handle login requests.");
		
	}
	
	/**
	 * Stops the server. This can only be ran when the server is in <code>ServerStatus.ONLINE</code> phase.
	 */
	public void stop() {
		if(status != ServerStatus.ONLINE) return;
		changeStatus(ServerStatus.UNLOADING);
		System.out.println("Stopping Server.");
		GameEngine.get().shutdown();
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
		logger = Logger.getLogger("Server");
		networkListenPort = port;
		statusHooks = new HashSet<>();
		commandHandler = new CommandHandler();
		networkManager = new NettyNetworkManager(new StreamListener() {
			@Override
			public void onDisconnect(NetworkStream networkStream) {
				logoutPlayer(networkStream.getPlayer());
			}
		});
		accountLoader = new AcceptingAccountLoader();
		
		players = new Player[MAX_PLAYERS];
		
	}
	
	public static void main(String[] args) {
		Server server = new Server(7000);
		server.start();
	}
	
}
