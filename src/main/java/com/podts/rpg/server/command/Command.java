package com.podts.rpg.server.command;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Command {
	
	private final String name;
	private final Set<String> aliases = new HashSet<>();
	private final Set<String> safeAliases = Collections.unmodifiableSet(aliases);
	
	private final int minArgs, maxArgs;
	
	public final String getName() {
		return name;
	}
	
	public final Collection<String> getAliases() {
		return safeAliases;
	}
	
	public final boolean execute(CommandSender sender, String commandEntry) {
		if(commandEntry == null || commandEntry.isEmpty()) return false;
		
		return doExecute(sender, commandEntry, new String[0]);
	}
	
	protected abstract boolean doExecute(CommandSender sender, String original, String[] parameters);
	
	public Command(String name, int minArgs, int maxArgs, String... aliases) {
		this.name = name;
		this.minArgs = minArgs;
		this.maxArgs = maxArgs;
		for(String a : aliases) {
			this.aliases.add(a);
		}
	}
	
	public Command(String name) {
		this(name, 0, 0);
	}
	
}
