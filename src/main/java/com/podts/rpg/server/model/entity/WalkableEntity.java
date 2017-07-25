package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.Location;

public class WalkableEntity extends Entity {
	
	public void walkForward() {
		setLocation(getFace().MoveFromLocation(getLocation()));
	}
	
	public WalkableEntity(EntityType type, Location loc) {
		super(type, loc);
	}

}
