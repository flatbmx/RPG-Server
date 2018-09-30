package com.podts.rpg.server.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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
	
	public final Optional<Command> getCommand(String name) {
		if(name == null || name.isEmpty()) return Optional.empty();
		lock();
		Command result = doGetCommand(name);
		unlock();
		return Optional.of(result);
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
		Optional<Command> command = getCommand(entry.name);
		if(!command.isPresent()) return;
		
		Server.get().getLogger().info(sender + " executing  " + commandText);
		command.get().doExecute(sender, commandText, entry.parameters);
		
	}
	
	private void clear() {
		lock();
		allCommands.clear();
		commandMap.clear();
		unlock();
	}
	
	private void initCommands() {
		
		addCommand(new Command("roll") {
			@Override
			protected boolean doExecute(CommandSender sender, String original, String[] parameters) {
				int low = 1, high = 100;
				if(parameters.length == 1) {
					try {
						int newHigh = Integer.parseInt(parameters[0]);
						if(newHigh > 0)
							high = newHigh;
					} catch(NumberFormatException e) {
						sender.sendMessage("Maximum was not an integer!");
						return true;
					}
				} else if(parameters.length == 2) {
					try {
						int newLow = Integer.parseInt(parameters[0]);
						int newHigh = Integer.parseInt(parameters[1]);
						if(newLow >= newHigh) {
							sender.sendMessage("low cannot be equal to or greater than high!");
							return true;
						}
					} catch(NumberFormatException e) {
						sender.sendMessage("Minimum or Maximum was not an integer!");
						return true;
					}
				} else if(parameters.length > 2) {
					return false;
				}
				Random r = new Random();
				int i = r.nextInt(high-low) + low;
				sender.sendMessage("You roll a " + i + " from " + low + " to " + high);
				return false;
			}
		});
		
		addCommand(new Command("refreshcommands") {
			@Override
			protected boolean doExecute(CommandSender sender, String original, String[] parameters) {
				clear();
				initCommands();
				return false;
			}
		});
		
		addCommand(new PlayerCommand("tile") {
			@Override
			protected boolean doExecute(Player player, String original, String[] parameters) {
				try {
					TileType type = TileType.valueOf(parameters[0].toUpperCase());
					Tile tile = player.getEntity().getTile();
					tile.getSpace().setTile(tile, type);
				} catch(Exception e) {
					player.sendMessage("No tile type called " + parameters[0] + "!");
				}
				return true;
			}
		});
		
		addCommand(new PlayerCommand("settile", 1, 1, "set", "settiles") {
			@Override
			protected boolean doExecute(Player player, String original, String[] parameters) {
				if(parameters.length != 1) {
					player.sendMessage("Incorrect amount of paremeters!");
				}
				
				TileType type = null;
				try {
					type = TileType.valueOf(parameters[0].toUpperCase());
				} catch(Exception e) {
					player.sendMessage("No tile type called " + parameters[0] + "!");
					return true;
				}
				
				Collection<Tile> tiles = player.getSelectedTiles();
				if(tiles.isEmpty()) {
					player.sendMessage("You have no tiles selected!");
					return true;
				}
				
				for(Tile tile : tiles) {
					tile.getSpace().setTile(tile, type);
				}
				
				player.sendMessage("Set all selected tiles to " + type + " type.");
				player.clearSelectedTiles();
				return true;
			}
		});
		
		addCommand(new PlayerCommand("growselection", 1 , 1, "growsel", "expandselection", "expandsel") {
			@Override
			protected boolean doExecute(Player player, String original, String[] parameters) {
				int size = 1;
				if(parameters.length == 1) {
					try {
						int newSize = Integer.parseInt(parameters[0]);
						if(newSize < 0 ) {
							player.sendMessage("size must be greater than 0.");
						}
						size = newSize;
					} catch(NumberFormatException e) {
						player.sendMessage("argument must be an integer!");
						return false;
					}
					Collection<Tile> newTiles = new HashSet<>();
					for(Tile tile : player.getSelectedTiles()) {
						Location topLeft = tile.getLocation().shift(-size, -size);
						size *= 2 + 1;
						for(int j=0; j<=size; ++j) {
							for(int i=0; i<=size; ++i) {
								newTiles.add(topLeft.shift(i, j).getTile());
							}
						}
					}
					player.setSelectedTiles(newTiles);
				}
				return false;
			}
		});
		
		addCommand(new PlayerCommand("pos") {
			@Override
			protected boolean doExecute(Player player, String original, String[] parameters) {
				player.sendMessage(player.getEntity().getLocation());
				return true;
			}
		});
		
		addCommand(new PlayerCommand("lpos") {
			@Override
			protected boolean doExecute(Player player, String original, String[] parameters) {
				Location point = player.getEntity().getLocation();
				player.sendMessage(point.getClass().getSimpleName() + " " + point);
				return true;
			}
		});
		
		addCommand(new PlayerCommand("setview") {
			@Override
			protected boolean doExecute(Player player, String original, String[] parameters) {
				try {
					if(parameters.length != 1) {
						player.sendMessage("Command requires one arugment for view size!");
						return true;
					}
					int newSize = Integer.parseInt(parameters[0]);
					player.setViewingDistance(newSize);
					player.sendMessage("Set new viewing distance to " + newSize);
				} catch(NumberFormatException e) {
					player.sendMessage("First arugement must be an integer!");
				}
				return true;
			}
		});
		
		addCommand(new PlayerCommand("teleport", 1, 3, "tp") {
			@Override
			protected boolean doExecute(Player player, String original, String[] parameters) {
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
		});
	}
	
	public CommandHandler() {
		
		initCommands();
		
	}
	
}
