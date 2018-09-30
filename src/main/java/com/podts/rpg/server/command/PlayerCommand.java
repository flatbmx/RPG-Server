package com.podts.rpg.server.command;

import java.util.Optional;

import com.podts.rpg.server.Player;

public abstract class PlayerCommand extends Command {
	
	private Optional<Player> getPlayer(CommandSender sender) {
		if(sender instanceof Player) {
			return Optional.of((Player) sender);
		}
		return Optional.empty();
	}
	
	@Override
	protected boolean doExecute(CommandSender sender, String original, String[] parameters) {
		Optional<Player> player = getPlayer(sender);
		if(!player.isPresent()) {
			sender.sendMessage(getSlashedName() + " can only be executed by players!");
			return true;
		}
		return doExecute(player.get(), original, parameters);
	}
	
	protected abstract boolean doExecute(Player player, String original, String[] parameteers);
	
	public PlayerCommand(String name, int minArgs, int maxArgs, String... aliases) {
		super(name, minArgs, maxArgs, aliases);
	}

	public PlayerCommand(String name) {
		super(name);
	}
	
}
