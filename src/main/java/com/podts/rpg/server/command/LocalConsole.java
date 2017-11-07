package com.podts.rpg.server.command;

public class LocalConsole extends Console {

	@Override
	public void sendMessage(String message) {
		//TODO implement using Logger
		throw new UnsupportedOperationException("Not implmented yet.");
	}

	@Override
	public void sendMessage(CommandSender sender, String message) {
		//TODO implement using Logger
		throw new UnsupportedOperationException("Not implmented yet.");
	}
	
	public LocalConsole() {
		super("Console");
	}
	
}
