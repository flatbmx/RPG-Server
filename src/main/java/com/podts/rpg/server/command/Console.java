package com.podts.rpg.server.command;

import java.util.logging.Logger;

/**
 * Represents a CommandSender that is not an actual game client.
 * Mainly textual but may have a GUI as well.
 *
 */
public abstract class Console implements CommandSender {
	
	protected final String name;
	protected final Logger logger;
	
	@Override
	public final String getName() {
		return name;
	}
	
	protected final Logger getLogger() {
		return logger;
	}
	
	@Override
	public void sendMessage(String message) {
		getLogger().info(message);
	}

	@Override
	public void sendMessage(CommandSender sender, String message) {
		getLogger().info(sender + ": " + message);
	}
	
	public Console(Logger logger, String name) {
		this.logger = logger;
		this.name = name;
	}
	
}
