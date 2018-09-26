package com.podts.rpg.server.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.Server;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.model.universe.TileElement.TileType;

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
		
		Server.get().getLogger().info(sender + " executing  " + commandText);
		command.doExecute(sender, commandText, entry.parameters);
		
	}
	
	private void clear() {
		lock();
		allCommands.clear();
		commandMap.clear();
		unlock();
	}
	
	private Player getPlayer(CommandSender sender) {
		if(sender instanceof Player) {
			return (Player) sender;
		}
		return null;
	}
	
	private void initCommands() {
		addCommand(new Command("refreshcommands") {
			@Override
			protected boolean doExecute(CommandSender sender, String original, String[] parameters) {
				clear();
				initCommands();
				return false;
			}
		});
		
		addCommand(new Command("tile") {
			@Override
			protected boolean doExecute(CommandSender sender, String original, String[] parameters) {
				if(parameters.length != 1) return false;
				if(sender instanceof Player) {
					TileType type = TileType.valueOf(parameters[0].toUpperCase());
					Player player = (Player) sender;
					Tile tile = player.getEntity().getTile();
					tile.getSpace().setTile(tile, type);
				}
				return true;
			}
		});
		
		addCommand(new Command("pos") {
			@Override
			protected boolean doExecute(CommandSender sender, String original, String[] parameters) {
				Player player = getPlayer(sender);
				if(player != null) {
					player.sendMessage(player.getEntity().getLocation());
				}
				return true;
			}
		});
		
		addCommand(new Command("lpos") {
			@Override
			protected boolean doExecute(CommandSender sender, String original, String[] parameters) {
				Player player = getPlayer(sender);
				if(player != null) {
					Location point = player.getEntity().getLocation();
					player.sendMessage(point.getClass().getSimpleName() + " " + point);
				}
				return true;
			}
		});
		
		addCommand(new Command("tp") {
			@Override
			protected boolean doExecute(CommandSender sender, String original, String[] parameters) {
				Player player = getPlayer(sender);
				if(player != null) {
					if(parameters.length == 1) {
						//Teleport to player
						String username = parameters[0];
						Player other = Server.get().getPlayer(username);
						if(other != null) {
							player.getEntity().getSpace().moveEntity(player.getEntity(), other.getEntity().getLocation(), MoveType.UPDATE);
							player.sendMessage("Teleported to " + username + " at " + other.getEntity().getLocation());
						}else
							player.sendMessage("No player found with name " + "\"" + username + "\"!");
						
						return true;
					}
					else if(parameters.length == 2 || parameters.length == 3) {
						//Teleport to coordinates
						try {
							int newX = Integer.parseInt(parameters[0]);
							int newY = Integer.parseInt(parameters[1]);
							int newZ = player.getEntity().getLocation().getZ();
							if(parameters.length == 3)
								newZ = Integer.parseInt(parameters[2]);
							Location newLoc = player.getEntity().getSpace().createLocation(newX, newY, newZ);
							
							newLoc.getSpace().moveEntity(player.getEntity(), newLoc, MoveType.UPDATE);
						} catch(NumberFormatException e) {
							player.sendMessage("One or more parameters were not integers!");
							return false;
						}
						return true;
					}
					return false;
				}
				return true;
			}
		});
	}
	
	public CommandHandler() {
		
		initCommands();
		
	}
	
}
