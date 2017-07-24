package com.podts.rpg.server.model.universe;

import java.util.HashMap;
import java.util.Map;

public final class Universe {
	
	private static Universe instance;
	
	public static final Universe get() {
		if(instance == null)
			instance = new Universe();
		return instance;
	}
	
	private final Map<String,World> worlds = new HashMap<String,World>();
	
	public synchronized World getWorld(String name) {
		return worlds.get(name);
	}
	
	/**
	 * Creates a new world with a given name.
	 * @param name - The name of the newly created world.
	 * @return The new world.
	 * @throws WorldAlreadyExistsException When there is already a world that has the given name.
	 */
	public synchronized World createWorld(String name) throws WorldAlreadyExistsException {
		final World other = getWorld(name);
		if(other != null) throw new WorldAlreadyExistsException(other);
		final World result = new StaticChunkWorld(name);
		worlds.put(name, result);
		return result;
	}
	
	/**
	 * Renames the given world.
	 * @param world - The World to be renamed.
	 * @param newName - The new name of the world.
	 * @throws WorldAlreadyExistsException When there is already a world named that currently.
	 */
	public synchronized final void renameWorld(World world, String newName) throws WorldAlreadyExistsException {
		if(world == null) throw new IllegalArgumentException();
		if(newName == null || newName.length() == 0) throw new IllegalArgumentException();
		final World other = getWorld(newName);
		if(other != null) throw new WorldAlreadyExistsException(other);
		worlds.remove(world.getName());
		world.setName(newName);
		worlds.put(newName, world);
	}
	
	public synchronized final void deleteWorld(World world) {
		worlds.remove(world.getName());
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
