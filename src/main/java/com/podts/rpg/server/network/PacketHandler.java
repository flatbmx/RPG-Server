package com.podts.rpg.server.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import com.podts.rpg.server.AccountLoader;
import com.podts.rpg.server.AccountLoader.AccountAlreadyExistsException;
import com.podts.rpg.server.AccountLoader.AccountDoesNotExistException;
import com.podts.rpg.server.AccountLoader.IncorrectPasswordException;
import com.podts.rpg.server.AccountLoader.InvalidUsernameException;
import com.podts.rpg.server.GameEngine;
import com.podts.rpg.server.GameStates;
import com.podts.rpg.server.Player;
import com.podts.rpg.server.Server;
import com.podts.rpg.server.command.Command;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.network.NetworkManager.NetworkStatus;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.LoginPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket.LoginResponseType;
import com.podts.rpg.server.network.packet.MessagePacket;
import com.podts.rpg.server.network.packet.PingPacket;
import com.podts.rpg.server.network.packet.RSAHandShakePacket;
import com.podts.rpg.server.network.packet.TileSelectionPacket;

public final class PacketHandler {
	
	private static Logger getLogger() {
		return Server.get().getLogger();
	}
	
	@FunctionalInterface
	public static interface PacketConsumer extends BiConsumer<NetworkStream,Packet> {}
	
	private static final Map<Class<? extends Packet>,PacketConsumer> handlers;
	
	static {
		handlers = new HashMap<>();
		
		handlers.put(PingPacket.class, (stream, oldPacket) -> {
			PingPacket packet = (PingPacket) oldPacket;
		});
		
		handlers.put(RSAHandShakePacket.class, (stream, oldPacket) -> {
				RSAHandShakePacket packet = (RSAHandShakePacket) oldPacket;
				AESReplyPacket reply = new AESReplyPacket(packet.getPublicKey(), stream.getSecretKey());
				stream.sendPacket(reply);
			}
		);
		
		handlers.put(MessagePacket.class, (stream, oldPacket) -> {
				MessagePacket packet = (MessagePacket) oldPacket;
				String message = packet.getMessage();
				if(Command.isPossibleCommand(message)) {
					Server.get().getCommandHandler().execute(packet.getSender(), message);
				} else {
					String formattedMessage = packet.getSender().getName() + ": " + message;
					getLogger().info(formattedMessage);
					Server.get().players()
					.forEach(p -> p.sendMessage(formattedMessage));
				}
			}
		);
		
		handlers.put(TileSelectionPacket.class, new PacketConsumer() {
			@Override
			public void accept(NetworkStream stream, Packet op) {
				TileSelectionPacket p = (TileSelectionPacket) op;
				Player player = stream.getPlayer();
				player.setSelectedTiles(p.getSelections(), false);
			}
		});
		
		handlers.put(EntityPacket.class, new PacketConsumer() {
			@Override
			public void accept(NetworkStream s, Packet packet) {
				EntityPacket p = (EntityPacket) packet;
				PlayerEntity pE = s.getPlayer().getEntity();
				Player player = pE.getPlayer();
				Entity e = p.getEntity();
				if(!pE.equals(e)) {
					getLogger().warning(player + " sent wront move ID packet!");
					return;
				}
				Location newLocation = p.getNewLocation();
				
				if(pE.distance(newLocation) >= 2) {
					//TODO TOO FAR
					getLogger().warning(player + " moved too far, probabbly out of sync!");
					return;
				}
				Optional<Direction> dir = Direction.get(pE.getLocation(), newLocation);
				if(!dir.isPresent()) {
					getLogger().warning(player + " moved not in a single direction!");
					return;
				}
				pE.move(dir.get());
			}
		});
		
		handlers.put(LoginPacket.class, new PacketConsumer() {
			@Override
			public void accept(NetworkStream networkStream, Packet oldPacket) {
				LoginPacket packet = (LoginPacket) oldPacket;
				
				if(!NetworkManager.networkManager.isOnline()) {
					NetworkManager.networkManager.addLoginRequest(packet);
					networkStream.sendPacket(new LoginResponsePacket(LoginResponseType.WAIT, "Server is loading, please wait."));
					return;
				}
				
				Player player = null;
				String response;
				LoginResponseType responseType = null;
				
				
				//TODO do NOT use exceptions for program flow.
				try {
					
					AccountLoader loader = Server.get().getAccountLoader();
					
					if(loader.accountExists(packet.getUsername())) {
						player = loader.loadAccount(packet.getUsername(), packet.getPassword());
					} else {
						try {
							player = loader.createAccount(packet.getUsername(), packet.getPassword());
						} catch (AccountAlreadyExistsException e) {
							e.printStackTrace();
						}
					}
					
					networkStream.setPlayer(player);
					responseType = LoginResponseType.ACCEPT;
					response = "Successfully logged in.";
				} catch (AccountDoesNotExistException e) {
					response = "Account not found!";
				} catch (IncorrectPasswordException e) {
					response = e.getMessage();
				} catch (InvalidUsernameException e) {
					response = e.getMessage();
				}
				
				if(responseType == null) responseType = LoginResponseType.DECLINE;
				
				networkStream.sendPacket(new LoginResponsePacket(responseType, response));

				if(LoginResponseType.DECLINE.equals(responseType)) {
					networkStream.closeStream();
					return;
				}
				
				//Login is accepted
				player.setStream(networkStream);
				Server.logger().info(player + " logged in.");
				player.changeGameState(GameStates.LOGGING_IN);
				
			}
		});
		
	}
	
	public static void handlePacket(Packet packet) {
		
		final PacketConsumer handler = handlers.get(packet.getClass());
		final NetworkStream networkStream = packet.getOrigin();
		
		//Server.get().getLogger().info("Recieved " + packet.getClass().getSimpleName() + " from " + packet.getOrigin().getPlayer());
		
		if(handler != null) {;
			GameEngine.get().submit(new PacketRunner(handler, packet, networkStream));
		} else {
			getLogger().warning("Recieved unhandled packet(" + packet.getClass().getSimpleName()
					+ ") from " + packet.getOrigin().ownerString());
		}
		
	}
	
	private static final class PacketRunner implements Runnable {
		
		private final Packet packet;
		private final NetworkStream networkStream;
		private final PacketConsumer handler;
		
		@Override
		public void run() {
			handler.accept(networkStream, packet);
		}
		
		private PacketRunner(PacketConsumer handler, Packet packet, NetworkStream networkStream) {
			this.handler = handler;
			this.packet = packet;
			this.networkStream = networkStream;
		}
		
	}
	
}
