package com.podts.rpg.server.command;

/**
 * Represents a CommandSender that is not an actual game client.
 * Mainly textual but may have a GUI as well.
 *
 */
public abstract class Console implements CommandSender {
	
	private final String name;
	
	@Override
	public String getName() {
		return name;
	}
	
	public Console(String name) {
		this.name = name;
	}
	
}
