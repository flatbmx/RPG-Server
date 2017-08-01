package com.podts.rpg.server.network.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.podts.rpg.server.Server;
import com.podts.rpg.server.account.AccountLoader.AccountDoesNotExistException;
import com.podts.rpg.server.account.AccountLoader.IncorrectPasswordException;
import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.LoginPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket.LoginResponseType;
import com.podts.rpg.server.network.packet.RSAHandShakePacket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DefaultPacketHandler extends SimpleChannelInboundHandler<Packet> {
	
	private static final Map<Class<? extends Packet>,BiConsumer<NettyStream,Packet>> handlers;
	
	static {
		
		handlers = new HashMap<Class<? extends Packet>,BiConsumer<NettyStream,Packet>>();
		
		handlers.put(RSAHandShakePacket.class, new BiConsumer<NettyStream,Packet>() {
			@Override
			public void accept(NettyStream stream, Packet packet) {
				RSAHandShakePacket rsaPacket = (RSAHandShakePacket) packet;
				Player player = new Player(stream);
				stream.player = player;
				AESReplyPacket reply = new AESReplyPacket(rsaPacket.getPublicKey(), stream.getSecretKey());
				stream.sendPacket(reply);
			}
		});
		
		handlers.put(LoginPacket.class, new BiConsumer<NettyStream,Packet>() {
			@Override
			public void accept(NettyStream stream, Packet packet) {
				LoginPacket p = (LoginPacket) packet;
				System.out.println("Recieved login | username: "+ p.getUsername() + " | password: " + p.getPassword());
				
				String response;
				LoginResponseType responseType;
				
				try {
					Player player = Server.get().getAccountLoader().loadAccount(p.getUsername(), p.getPassword());
					stream.player = player;
					responseType = LoginResponseType.ACCEPT;
					response = "Successfully logged in.";
				} catch (AccountDoesNotExistException e) {
					response = "Account not found!";
					responseType = LoginResponseType.DECLINE;
				} catch (IncorrectPasswordException e) {
					response = "Incorrect password!";
					responseType = LoginResponseType.DECLINE;
				}
				
				//stream.sendPacket(new LoginResponsePacket(responseType, response));
				
				if(responseType.equals(LoginResponseType.DECLINE)) {
					stream.close();
				}
				
			}
		});
		
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext context, Packet packet) throws Exception {
		
		NettyStream stream = (NettyStream) context.channel();
		
		BiConsumer<NettyStream,Packet> handler = handlers.get(packet.getClass());
		
		if(handler != null) {
			handler.accept(stream, packet);
		} else {
			System.out.println("Recieved unhandled packet " + packet.getClass().getSimpleName());
		}
		
	}
	
}
