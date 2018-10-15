package com.podts.rpg.server.network.packet;

public class PingPacket extends AcknowledgementPacket {
	
	public PingPacket(int ack) {
		super(ack);
	}
	
	public PingPacket(PingPacket packet) {
		this(packet.getACK());
	}
	
}
