package com.podts.rpg.server.command;

public interface CommandSender {
	
	public String getName();
	public void sendMessage(String message);
	public void sendMessage(CommandSender sender, String message);
	
}
