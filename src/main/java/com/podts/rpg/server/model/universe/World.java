package com.podts.rpg.server.model.universe;

import java.util.Collection;

import com.podts.rpg.server.model.Locatable;
import com.podts.rpg.server.model.entity.Entity;

public abstract class World {
	
	private String name;
	
	public final String getName() {
		return name;
	}
	
	protected final void setName(String newName) {
		name = newName;
	}
	
	public abstract Tile getTile(int x, int y, int z);
	public abstract void setTile(Tile newTile, int x, int y, int z);
	
	public abstract Collection<Entity> getNearbyEntities(Locatable l, double distance);
	
	public abstract boolean register(Entity e);
	
	public String toString() {
		return "World - " + name;
	}
	
	protected World(String name) {
		this.name = name;
	}
	
}
