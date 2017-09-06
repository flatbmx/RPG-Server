package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;

public class Entity implements Locatable {
	
	private static int nextID;
	
	private final int id;
	private String name;
	private final EntityType type;
	private Location location;
	
	public final int getID() {
		return id;
	}
	
	public final EntityType getType() {
		return type;
	}
	
	public final boolean isPlayer() {
		return EntityType.PLAYER.equals(type);
	}
	
	public final String getName() {
		return name;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public final Entity move(int dx, int dy, int dz) {
		getLocation().getWorld().moveEntity(this, MoveType.UPDATE, dx, dy, dz);
		return this;
	}
	
	public final Entity move(Direction dir) {
		getLocation().getWorld().moveEntity(this, dir);
		return this;
	}
	
	protected final Entity setLocation(Location newLocation) {
		location = newLocation;
		return this;
	}
	
	public final boolean register() {
		return location.getWorld().register(this);
	}
	
	public final void deRegister() {
		location.getWorld().deRegister(this);
	}
	
	public Entity(EntityType type, Location loc) {
		id = nextID++;
		name = type.name();
		this.type = type;
		location = loc;
	}
	
	public Entity(String name, EntityType type, Location loc) {
		id = nextID++;
		this.name = name;
		this.type = type;
		this.location = loc;
	}
	
}
