package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Location;

public abstract class LivingEntity extends FightableEntity implements Livable {
	
	public LivingEntity(String name, EntityType type, Location loc) {
		super(name, type, loc);
	}
	
}
