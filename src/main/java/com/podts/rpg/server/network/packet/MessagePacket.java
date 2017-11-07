package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.command.CommandSender;
import com.podts.rpg.server.network.Packet;

public class MessagePacket extends Packet {
	
	private final CommandSender sender;
	private final String message;
	
	public final CommandSender getSender() {
		return sender;
	}
	
	public final String getMessage() {
		return message;
	}
	
	public MessagePacket(CommandSender sender, String message) {
		this.sender = sender;
		this.message = message;
	}
	
	public MessagePacket(String message) {
		this(null, message);
	}
	
}
