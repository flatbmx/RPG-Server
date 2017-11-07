package com.podts.rpg.server.command;

import com.podts.rpg.server.network.Stream;
import com.podts.rpg.server.network.packet.MessagePacket;

public class NetworkConsole extends Console {
	
	private final Stream stream;
	
	Stream getStream() {
		return stream;
	}
	
	@Override
	public void sendMessage(String message) {
		//TODO Implement using Logger that logs to file and network.
		getStream().sendPacket(new MessagePacket(message));
	}
	
	@Override
	public void sendMessage(CommandSender sender, String message) {
		//TODO Implement using Logger that logs to file and network.
		getStream().sendPacket(new MessagePacket(sender, message));
	}
	
	public NetworkConsole(Stream stream) {
		super(stream.getAddress().getHostAddress());
		this.stream = stream;
	}
	
}
