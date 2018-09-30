package com.podts.rpg.server.command;

import java.util.logging.Level;
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
	
	public void sendMessage(Level level, String message) {
		getLogger().log(level, message);
	}
	
	public void sendMessage(Level level, CommandSender sender, String message) {
		getLogger().log(level, sender + ": " + message);
	}
	
	@Override
	public void sendMessage(String message) {
		getLogger().info(message);
	}

	@Override
	public void sendMessage(CommandSender sender, String message) {
		sendMessage(sender + ": " + message);
	}
	
	public Console(Logger logger, String name) {
		this.logger = logger;
		this.name = name;
	}
	
}
