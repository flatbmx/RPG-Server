package com.podts.rpg.server.network;

import java.util.HashMap;
import java.util.Map;
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
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.network.NetworkManager.NetworkStatus;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.LoginPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket.LoginResponseType;
import com.podts.rpg.server.network.packet.MessagePacket;
import com.podts.rpg.server.network.packet.RSAHandShakePacket;

public final class PacketHandler {
	
	private static Logger getLogger() {
		return Server.get().getLogger();
	}
	
	@FunctionalInterface
	public static interface PacketConsumer extends BiConsumer<NetworkStream,Packet> {}
	
	private static final Map<Class<? extends Packet>,PacketConsumer> handlers;
	
	static {
		handlers = new HashMap<>();
		
		handlers.put(RSAHandShakePacket.class, (stream, oldPacket) -> {
				RSAHandShakePacket packet = (RSAHandShakePacket) oldPacket;
				AESReplyPacket reply = new AESReplyPacket(packet.getPublicKey(), stream.getSecretKey());
				stream.sendPacket(reply);
			}
		);
		
		handlers.put(MessagePacket.class, (stream, oldPacket) -> {
				MessagePacket packet = (MessagePacket) oldPacket;
				String message = packet.getMessage();
				if(message.startsWith("/")) {
					Server.get().getCommandHandler().execute(packet.getSender(), message);
				} else {
					String newMessage = packet.getSender().getName() + ": " + message;
					stream.getPlayer().getEntity().getSpace().players()
					.forEach(p -> p.sendMessage(newMessage));
					getLogger().info(newMessage);
				}
			}
		);
		
		handlers.put(EntityPacket.class, new PacketConsumer() {
			@Override
			public void accept(NetworkStream s, Packet packet) {
				EntityPacket p = (EntityPacket) packet;
				PlayerEntity pE = s.getPlayer().getEntity();
				Entity e = p.getEntity();
				if(!pE.equals(e)) {
					getLogger().warning(pE.getPlayer() + " sent wront move ID packet!");
					return;
				}
				Location newLocation = p.getNewLocation();
				
				if(pE.distance(newLocation) > 1) {
					//TODO TOO FAR
					getLogger().warning("Far");
					return;
				}
				Direction dir = Direction.get(pE.getLocation(), newLocation);
				if(dir == null) {
					getLogger().warning("Diag");
					//TODO Diagonal, not valid.
					return;
				}
				pE.move(dir);
			}
		});
		
		handlers.put(LoginPacket.class, new PacketConsumer() {
			@Override
			public void accept(NetworkStream networkStream, Packet oldPacket) {
				LoginPacket packet = (LoginPacket) oldPacket;
				
				if(!NetworkStatus.ONLINE.equals(NetworkManager.networkManager.getStatus())) {
					NetworkManager.networkManager.addLoginRequest(packet);
					networkStream.sendPacket(new LoginResponsePacket(LoginResponseType.WAIT, "Server is loading, please wait."));
					return;
				}
				
				getLogger().info("Recieved login | username: "+ packet.getUsername() + " | password: " + packet.getPassword());
				
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
				player.changeGameState(GameStates.LOGGING_IN);
				
			}
		});
		
	}
	
	public static void handlePacket(Packet packet) {
		
		PacketConsumer handler = handlers.get(packet.getClass());
		
		final NetworkStream networkStream = packet.getOrigin();
		
		if(handler != null) {
			//System.out.println("Recieved " + packet.getClass().getSimpleName());
			GameEngine.get().submit(new PacketRunner(handler, packet, networkStream));
		} else {
			getLogger().warning("Recieved unhandled packet " + packet.getClass().getSimpleName());
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
