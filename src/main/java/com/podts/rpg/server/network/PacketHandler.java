package com.podts.rpg.server.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.podts.rpg.server.GameEngine;
import com.podts.rpg.server.Server;
import com.podts.rpg.server.account.AccountLoader;
import com.podts.rpg.server.account.AccountLoader.AccountAlreadyExistsException;
import com.podts.rpg.server.account.AccountLoader.AccountDoesNotExistException;
import com.podts.rpg.server.account.AccountLoader.IncorrectPasswordException;
import com.podts.rpg.server.account.AccountLoader.InvalidUsernameException;
import com.podts.rpg.server.model.GameState;
import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.PlayerEntity;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.model.universe.World;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.network.NetworkManager.NetworkStatus;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.LoginPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket.LoginResponseType;
import com.podts.rpg.server.network.packet.PlayerInitPacket;
import com.podts.rpg.server.network.packet.RSAHandShakePacket;
import com.podts.rpg.server.network.packet.StatePacket;

public final class PacketHandler {
	
	private static final Map<Class<? extends Packet>,BiConsumer<Stream,Packet>> handlers;
	
	static {
		handlers = new HashMap<>();
		
		handlers.put(RSAHandShakePacket.class, new BiConsumer<Stream,Packet>() {
			@Override
			public void accept(Stream stream, Packet packet) {
				RSAHandShakePacket rsaPacket = (RSAHandShakePacket) packet;				
				AESReplyPacket reply = new AESReplyPacket(rsaPacket.getPublicKey(), stream.getSecretKey());
				stream.sendPacket(reply);
			}
		});
		
		handlers.put(EntityPacket.class, new BiConsumer<Stream,Packet>() {
			@Override
			public void accept(Stream s, Packet packet) {
				EntityPacket p = (EntityPacket) packet;
				PlayerEntity pE = s.getPlayer().getEntity();
				Entity e = p.getEntity();
				if(!pE.equals(e)) {
					System.out.println("Player sent wront move ID packet!");
					return;
				}
				Location newLocation = p.getNewLocation();
				
				if(pE.distance(newLocation) > 1) {
					//TODO TOO FAR
					System.out.println("Far");
					return;
				}
				Direction dir = Direction.getFromLocations(pE.getLocation(), newLocation);
				if(dir == null) {
					System.out.println("Diag");
					//TODO Diagonal, not valid.
					return;
				}
				pE.move(dir);
			}
		});
		
		handlers.put(LoginPacket.class, new BiConsumer<Stream,Packet>() {
			@Override
			public void accept(Stream stream, Packet oldPacket) {
				LoginPacket packet = (LoginPacket) oldPacket;
				
				if(!NetworkStatus.ONLINE.equals(NetworkManager.networkManager.getStatus())) {
					NetworkManager.networkManager.addLoginRequest(packet);
					stream.sendPacket(new LoginResponsePacket(LoginResponseType.WAIT, "Server is loading, please wait."));
					return;
				}
				
				System.out.println("Recieved login | username: "+ packet.getUsername() + " | password: " + packet.getPassword());
				
				Player player = null;
				String response;
				LoginResponseType responseType = null;

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
					
					stream.setPlayer(player);
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
				
				stream.sendPacket(new LoginResponsePacket(responseType, response));

				if(responseType.equals(LoginResponseType.DECLINE)) {
					stream.closeStream();
					return;
				}

				World world = Universe.get().getDefaultWorld();
				player.setStream(stream);
				player.getStream().sendPacket(new PlayerInitPacket(player));
				world.register(player.getEntity());
				
				stream.sendPacket(new StatePacket(GameState.PLAYING));
				
			}
		});
		
	}
	
	public static void handlePacket(Packet packet) {
		
		BiConsumer<Stream,Packet> handler = handlers.get(packet.getClass());
		
		final Stream stream = packet.getOrigin();
		
		if(handler != null) {
			System.out.println("Recieved " + packet.getClass().getSimpleName());
			GameEngine.get().submit(new PacketRunner(handler, packet, stream));
		} else {
			System.out.println("Recieved unhandled packet " + packet.getClass().getSimpleName());
		}
		
	}
	
	private static final class PacketRunner implements Runnable {
		private final Packet packet;
		private final Stream stream;
		private final BiConsumer<Stream,Packet> handler;
		@Override
		public void run() {
			handler.accept(stream, packet);
		}
		PacketRunner(BiConsumer<Stream,Packet> handler, Packet packet, Stream stream) {
			this.handler = handler;
			this.packet = packet;
			this.stream = stream;
		}
	}
	
}
