package com.podts.rpg.server.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

public final class CommandHandler {
	
	private final Map<String,Command> commandMap = new HashMap<>();
	private final Set<Command> allCommands = new HashSet<>();
	private final Semaphore lock = new Semaphore(1);
	
	private final void lock() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {}
	}
	
	private final void unlock() {
		lock.release();
	}
	
	private final String formatName(String name) {
		return name.toLowerCase();
	}
	
	private final boolean isValidCommand(Command command) {
		if(command == null) return false;
		if(command.getName() == null || command.getName().isEmpty()) return false;
		for(String alias : command.getAliases())
			if(alias == null || alias.isEmpty()) return false;
		return true;
	}
	
	public final Command getCommand(String name) {
		if(name == null || name.isEmpty()) return null;
		lock();
		Command result = doGetCommand(name);
		unlock();
		return result;
	}
	
	private final Command doGetCommand(String name) {
		return commandMap.get(formatName(name));
	}
	
	public final boolean addCommand(Command newCommand) {
		if(!isValidCommand(newCommand)) return false;
		lock();
		if(doGetCommand(newCommand.getName()) != null) {
			unlock();
			return false;
		}
		for(String alias : newCommand.getAliases()) {
			if(doGetCommand(alias) != null) {
				unlock();
				return false;
			}
		}
		
		allCommands.add(newCommand);
		commandMap.put(formatName(newCommand.getName()), newCommand);
		for(String alias : newCommand.getAliases())
			commandMap.put(formatName(alias), newCommand);
		unlock();
		return true;
	}
	
	public final void removeCommand(Command command) {
		lock();
		if(!allCommands.remove(command)) {
			unlock();
			return;
		}
		commandMap.remove(formatName(command.getName()));
		for(String alias : command.getAliases())
			commandMap.remove(formatName(alias));
		unlock();
	}
	
	public final void execute(CommandSender sender, String commandText) {
		
		CommandEntry entry = CommandParser.parse(commandText);
		if(entry == null) return;
		Command command = getCommand(entry.name);
		if(command == null) return;
		
		command.doExecute(sender, commandText, entry.parameters);
		
	}
	
}
