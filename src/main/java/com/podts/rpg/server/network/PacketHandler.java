package com.podts.rpg.server.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.podts.rpg.server.GameEngine;
import com.podts.rpg.server.Server;
import com.podts.rpg.server.account.AccountLoader.AccountDoesNotExistException;
import com.podts.rpg.server.account.AccountLoader.IncorrectPasswordException;
import com.podts.rpg.server.account.AccountLoader.InvalidUsernameException;
import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.model.universe.World;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.LoginPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket.LoginResponseType;
import com.podts.rpg.server.network.packet.PlayerInitPacket;
import com.podts.rpg.server.network.packet.RSAHandShakePacket;

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
		
		handlers.put(LoginPacket.class, new BiConsumer<Stream,Packet>() {
			@Override
			public void accept(Stream stream, Packet packet) {
				LoginPacket p = (LoginPacket) packet;
				System.out.println("Recieved login | username: "+ p.getUsername() + " | password: " + p.getPassword());
				
				Player player = null;
				String response;
				LoginResponseType responseType = null;

				try {
					player = Server.get().getAccountLoader().loadAccount(p.getUsername(), p.getPassword());
					stream.setPlayer(player);
					responseType = LoginResponseType.ACCEPT;
					response = "Successfully logged in.";
				} catch (AccountDoesNotExistException e) {
					response = "Account not found!";
				} catch (IncorrectPasswordException e) {
					response = "Incorrect password!";
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
				
				
				
			}
		});
		
	}
	
	public static void handlePacket(Packet packet, Stream stream) {
		
		BiConsumer<Stream,Packet> handler = handlers.get(packet.getClass());
		
		if(handler != null) {
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
