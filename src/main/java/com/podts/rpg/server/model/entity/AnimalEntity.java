package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Location;

public abstract class AnimalEntity extends LivingEntity implements Animal {
	
	private boolean isAwake = true;
	
	public boolean isAwake() {
		return isAwake;
	}
	
	public AnimalEntity(String name, EntityType type, Location loc) {
		super(name, type, loc);
	}
	
}
