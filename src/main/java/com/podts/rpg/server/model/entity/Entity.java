package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.Locatable;
import com.podts.rpg.server.model.Location;

public class Entity implements Locatable {
	
	private static int nextID;
	
	private final int id;
	private final EntityType type;
	private Location location;
	
	public final int getID() {
		return id;
	}
	
	public final EntityType getType() {
		return type;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Entity(EntityType type, Location loc) {
		id = nextID++;
		this.type = type;
		location = loc;
	}
	
}
