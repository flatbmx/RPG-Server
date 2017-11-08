package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.network.Packet;

public class AcknowledgePacket extends Packet {
	
	private final int ack;
	
	public final int getACK() {
		return ack;
	}
	
	public AcknowledgePacket(int ack) {
		this.ack = ack;
	}
	
	public AcknowledgePacket(AcknowledgementPacket packet) {
		this(packet.getACK());
	}
	
}
