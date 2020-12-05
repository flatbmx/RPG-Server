package com.podts.rpg.server.model.universe;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.universe.region.PollableRegion;

public final class Universe {
	
	private static Universe instance;
	
	public static final Universe get() {
		if(instance == null)
			instance = new Universe();
		return instance;
	}
	
	private final Map<String,World> worlds = new HashMap<String,World>();
	
	private World defaultWorld;
	
	public World getDefaultWorld() {
		return defaultWorld;
	}
	
	public synchronized World getWorld(String name) {
		return worlds.get(name);
	}
	
	/**
	 * Creates a new world with a given name.
	 * @param name - The name of the newly created world.
	 * @param generator - The WorldGenerator that this world will use to generate new Tiles.
	 * @return The new world.
	 * @throws WorldAlreadyExistsException When there is already a world that has the given name.
	 */
	public synchronized World createWorld(String name, WorldGenerator generator) throws WorldAlreadyExistsException {
		if(name == null) throw new IllegalArgumentException("Cannot create World with null name.");
		if(generator == null) throw new IllegalArgumentException("Cannot create World with null WorldGenerator.");
		final World other = getWorld(name);
		if(other != null) throw new WorldAlreadyExistsException(other);
		
		final World result = new StaticChunkWorld(name, generator);
		worlds.put(result.getName(), result);
		if(defaultWorld == null) defaultWorld = result;
		return result;
	}
	
	/**
	 * Renames the given world.
	 * @param world - The World to be renamed.
	 * @param newName - The new name of the world.
	 * @throws WorldAlreadyExistsException When there is already a world named that currently.
	 */
	public synchronized final Universe renameWorld(World world, String newName) throws WorldAlreadyExistsException {
		if(world == null) throw new IllegalArgumentException("Cannot rename a null world.");
		if(!worlds.containsKey(world.getName())) throw new IllegalArgumentException("Cannot rename deleted world.");
		if(newName == null || newName.length() == 0) throw new IllegalArgumentException("Cannot rename a world with null or empty name.");
		if(world.getName().equals(newName)) return this;
		
		//See if there is another world with the same name.
		final World other = getWorld(newName);
		if(other != null) throw new WorldAlreadyExistsException(other);
		
		
		worlds.remove(world.getName());
		world.setName(newName);
		worlds.put(newName, world);
		return this;
	}
	
	public synchronized final Universe deleteWorld(World world) {
		worlds.remove(world.getName());
		return this;
	}
	
	public Stream<World> worlds() {
		return worlds.values().stream();
	}
	
	public Stream<Tile> tiles() {
		return worlds()
				.flatMap(World::tiles);
	}
	
	public Stream<Entity> entities() {
		return worlds()
				.flatMap(World::entities);
	}
	
	public Stream<PollableRegion> regions() {
		return worlds()
				.flatMap(World::regions);
	}
	
	private Universe() {
		
	}
	
	public static class WorldAlreadyExistsException extends Exception {
		
		private static final long serialVersionUID = -8478741567587963839L;
		
		private final World world;
		
		public final World getWorld() {
			return world;
		}
		
		protected WorldAlreadyExistsException(World world) {
			this.world = world;
		}
		
		protected WorldAlreadyExistsException(World world, String message) {
			super(message);
			this.world = world;
		}
		
	}
	
}
