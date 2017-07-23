package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.network.Packet;

public class MessagePacket extends Packet {
	
	private final String message;
	
	public final String getMessage() {
		return message;
	}
	
	public MessagePacket(String message) {
		this.message = message;
	}
	
}
