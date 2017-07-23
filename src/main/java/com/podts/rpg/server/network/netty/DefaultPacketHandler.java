package com.podts.rpg.server.network.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.ship.ShipFactory;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.AESReplyPacket;
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
				Player player = new Player(stream, ShipFactory.getStartingShip());
				stream.player = player;
				AESReplyPacket reply = new AESReplyPacket(player, rsaPacket.getPublicKey(), stream.getSecretKey());
				stream.sendPacket(reply);
			}
		});
		
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext context, Packet packet) throws Exception {
		
		NettyStream stream = (NettyStream) context.channel();
		
		BiConsumer<NettyStream,Packet> handler = handlers.get(packet.getClass());
		
		if(handlers != null) {
			handler.accept(stream, packet);
		} else {
			
		}
		
	}
	
}
