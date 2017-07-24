package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.Locatable;
import com.podts.rpg.server.model.Location;

public class Entity implements Locatable {
	
	private static int nextID;
	
	public enum Face {
		UP(0,-1),
		DOWN(0,1),
		LEFT(-1,0),
		RIGHT(1,0);
		
		private final int dx, dy;
		
		public Location MoveFromLocation(Location origin) {
			return origin.move(dx, dy, 0);
		}
		
		private Face(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
		
	}
	
	private final int id;
	private final EntityType type;
	private Location location;
	private Face face = Face.DOWN;
	
	public final int getID() {
		return id;
	}
	
	public final EntityType getType() {
		return type;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Face getFace() {
		return face;
	}
	
	protected Entity setLocation(Location newLocation) {
		location = newLocation;
		return this;
	}
	
	public Entity(EntityType type, Location loc) {
		id = nextID++;
		this.type = type;
		location = loc;
	}
	
}
