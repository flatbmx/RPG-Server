package com.podts.rpg.server.model.universe;

import java.util.Collection;

import com.podts.rpg.server.model.Locatable;
import com.podts.rpg.server.model.Location;
import com.podts.rpg.server.model.entity.Entity;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.model.universe.region.SimpleRegionHandler;

public abstract class World extends SimpleRegionHandler implements Region {
	
	private final WorldGenerator generator;
	private String name;
	
	public final String getName() {
		return name;
	}
	
	protected final void setName(String newName) {
		name = newName;
	}
	
	public final WorldGenerator getWorldGenerator() {
		return generator;
	}
	
	public abstract Tile getTile(Locatable point);
	public abstract World setTile(Tile newTile, Location point);
	
	public abstract Collection<Entity> getNearbyEntities(Locatable l, double distance);
	public abstract Collection<Entity> getNearbyEntities(Locatable l);
	
	public abstract boolean register(Entity e);
	public abstract World deRegister(Entity e);
	
	public abstract Collection<Region> getRegionsAtLocation(Locatable loc);
	
	@Override
	public final boolean contains(Locatable loc) {
		if(loc == null) throw new IllegalArgumentException("Cannot determine if null Locatable is in World.");
		return equals(loc.getWorld());
	}
	
	@Override
	public String toString() {
		return "World - " + name;
	}
	
	protected World(String name, WorldGenerator generator) {
		this.name = name;
		this.generator = generator;
	}
	
}
