package com.podts.rpg.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.podts.rpg.server.Player.LogoutReason;
import com.podts.rpg.server.account.AcceptingAccountLoader;
import com.podts.rpg.server.command.CommandHandler;
import com.podts.rpg.server.command.GameLogger;
import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.model.universe.Universe.WorldAlreadyExistsException;
import com.podts.rpg.server.model.universe.generators.PerlinNoiseGenerator;
import com.podts.rpg.server.network.NetworkManager;
import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.NetworkStreamListener;
import com.podts.rpg.server.network.netty.NettyNetworkManager;

public final class Server {
	
	@FunctionalInterface
	public static interface ServerStatusHook extends BiConsumer<ServerStatus,ServerStatus> {}
	
	/**
	 * Stores the instance of the server.
	 */
	private static Server instance;
	
	/**
	 * Get the current instance of the server running in this JVM.
	 * @return The current server instance.
	 */
	public static final Server get() {
		return instance;
	}
	
	public static final Logger logger() {
		return get().getLogger();
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
	private final Set<ServerStatusHook> statusHooks;
	private final Set<PlayerLoginListener> playerLoginListeners = new HashSet<>();
	private int networkListenPort;
	
	private final NetworkManager networkManager;
	private final AccountLoader accountLoader;
	private final CommandHandler commandHandler;
	
	private final Player[] players;
	private final Map<String,Player> playerNameMap = new HashMap<>();
	private final Collection<Player> safePlayers = Collections.unmodifiableCollection(playerNameMap.values());
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
	
	public boolean addStatusHook(ServerStatusHook hook) {
		if(hook == null) throw new IllegalArgumentException("Cannot add a null Shutdown Hook.");
		return statusHooks.add(hook);
	}
	
	public boolean removeStatusHook(ServerStatusHook hook) {
		if(hook == null) throw new IllegalArgumentException("Cannot remove a null Shutdown Hook.");
		return statusHooks.remove(hook);
	}
	
	public boolean addPlayerLoginListener(PlayerLoginListener listener) {
		if(listener == null) throw new NullPointerException("Cannot add a null PlayerLoginListener.");
		return playerLoginListeners.add(listener);
	}
	
	public boolean removePlayerLoginListener(PlayerLoginListener listener) {
		if(listener == null) throw new NullPointerException("Cannot remove a null PlayerLoginListener.");
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
	
	public Player getPlayer(String name) {
		return playerNameMap.get(name);
	}
	
	public Collection<Player> getPlayers() {
		return safePlayers;
	}
	
	public Stream<Player> players() {
		return getPlayers().stream();
	}
	
	Player createPlayer(String username, String password) {
		Player player = new Player(getNewID(), username, password);
		players[player.getID()] = player;
		playerNameMap.put(username, player);
		return player;
	}
	
	void handleLogin(Player player) {
		playerLoginListeners.forEach(l -> l.onLogin(player));
	}
	
	public void logoutPlayer(Player player, LogoutReason reason) {
		player.getEntity().deRegister();
		for(PlayerLoginListener listener : playerLoginListeners) {
			listener.onLogout(player, reason);
		}
		int id = player.getID();
		players[id] = null;
		playerNameMap.remove(player.getUsername());
		recycledPlayerIDs.add(id);
		getLogger().info(player.getUsername() + " logged out.");
	}
	
	/**
	 * Starts the server, the server can only be started in the <code>ServerStatus.OFFLINE</code> phase.
	 */
	public void start() {
		if(status != ServerStatus.OFFLINE) return;
		changeStatus(ServerStatus.LOADING);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		
		if (!networkManager.bind(networkListenPort)) {
			getLogger().severe("Server failed to bind to " + networkManager.getPort());
			changeStatus(ServerStatus.OFFLINE);
			return;
		}
		
		getLogger().info("Server bound to " + networkManager.getBoundAddressWithPort());
		
		GameEngine.create(4);
		
		try {
			Universe.get().createWorld("Earth", new PerlinNoiseGenerator());
		} catch (WorldAlreadyExistsException e) {
			e.printStackTrace();
		}
		
		changeStatus(ServerStatus.ONLINE);
		getLogger().info("Server is now online and can handle login requests.");
		
	}
	
	/**
	 * Stops the server. This can only be ran when the server is in <code>ServerStatus.ONLINE</code> phase.
	 */
	public void stop() {
		if(status != ServerStatus.ONLINE) return;
		changeStatus(ServerStatus.UNLOADING);
		getLogger().info("Stopping Server.");
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
		logger = new GameLogger("Server");
		networkListenPort = port;
		statusHooks = new HashSet<>();
		commandHandler = new CommandHandler();
		networkManager = new NettyNetworkManager(new NetworkStreamListener() {
			@Override
			public void onDisconnect(NetworkStream networkStream) {
				Server.logger().info(networkStream + " disconnected ");
				logoutPlayer(networkStream.getPlayer(), LogoutReason.DISCONNECT);
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
