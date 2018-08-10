package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.network.Packet;

public abstract class AcknowledgementPacket extends Packet {
	
	private final int ack;
	
	public final int getACK() {
		return ack;
	}
	
	AcknowledgementPacket(int ack) {
		this.ack = ack;
	}
	
}
