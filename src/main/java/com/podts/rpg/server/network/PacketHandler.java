package com.podts.rpg.server.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.podts.rpg.server.GameEngine;
import com.podts.rpg.server.Server;
import com.podts.rpg.server.account.AccountLoader.AccountDoesNotExistException;
import com.podts.rpg.server.account.AccountLoader.IncorrectPasswordException;
import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.model.universe.World;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.LoginPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket.LoginResponseType;
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
				
				GameEngine.get().submit(new Runnable() {
					@Override
					public void run() {

						Player player = null;
						String response;
						LoginResponseType responseType;

						try {
							player = Server.get().getAccountLoader().loadAccount(p.getUsername(), p.getPassword());
							stream.setPlayer(player);
							responseType = LoginResponseType.ACCEPT;
							response = "Successfully logged in.";
						} catch (AccountDoesNotExistException e) {
							response = "Account not found!";
							responseType = LoginResponseType.DECLINE;
						} catch (IncorrectPasswordException e) {
							response = "Incorrect password!";
							responseType = LoginResponseType.DECLINE;
						}

						stream.sendPacket(new LoginResponsePacket(responseType, response));

						if(responseType.equals(LoginResponseType.DECLINE)) {
							stream.closeStream();
						}

						World world = Universe.get().getDefaultWorld();
						PlayerEntity pE = new PlayerEntity(player, world.createLocation(0, 0, 0));
						world.register(pE);
					}
				});
			}
		});
		
	}
	
	public static void handlePacket(Packet packet, Stream stream) {
		
		BiConsumer<Stream,Packet> handler = handlers.get(packet.getClass());
		
		if(handler != null) {
			handler.accept(stream, packet);
		} else {
			System.out.println("Recieved unhandled packet " + packet.getClass().getSimpleName());
		}
		
	}
	
}
