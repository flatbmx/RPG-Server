package com.podts.rpg.server.command;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

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
	
	public Stream<String> aliases() {
		return getAliases().stream();
	}
	
	public final int getMinimumArguments() {
		return minArgs;
	}
	
	public final int getMaximumArguments() {
		return maxArgs;
	}
	
	public final boolean execute(CommandSender sender, String commandEntry) {
		if(commandEntry == null || commandEntry.isEmpty()) return false;
		return doExecute(sender, commandEntry, new String[0]);
	}
	
	protected abstract boolean doExecute(CommandSender sender, String original, String[] parameters);
	
	public Command(String name, int minArgs, int maxArgs, String... aliases) {
		this.name = Objects.requireNonNull(name, "Cannot construct Command with null name!");
		if(name.isEmpty()) throw new IllegalArgumentException("Cannot construct Command with empty name!");
		this.minArgs = minArgs;
		this.maxArgs = maxArgs;
		for(String a : aliases) {
			if(Objects.requireNonNull(a, "Cannot construct Command with a null alias!").isEmpty())
				throw new IllegalArgumentException("Cannot construct Command with empty alias!");
			this.aliases.add(a);
		}
	}
	
	public Command(String name) {
		this(name, 0, 0);
	}
	
}
