package com.podts.rpg.server.network.netty;

import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

class DefaultPacketHandler extends SimpleChannelInboundHandler<Packet> {
	
	@Override
	protected void channelRead0(ChannelHandlerContext context, Packet packet) throws Exception {
		
		PacketHandler.handlePacket(packet);
		
	}
	
}
