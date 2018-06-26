package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

public abstract class LivingHumanoid extends AnimalEntity implements Mammal, CanSee {
	
	private double viewingDistance;
	
	@Override
	public boolean isAlive() {
		return getHP() > 0;
	}
	
	@Override
	public final double getViewingDistance() {
		return viewingDistance;
	}
	
	public LivingHumanoid setViewingDistance(double viewingDistance) {
		this.viewingDistance = viewingDistance;
		return this;
	}
	
	public boolean sees(Locatable loc) {
		if(!isInPlane(loc)) return false;
		return isInRange(loc, getViewingDistance());
	}
	
	public LivingHumanoid(String name, EntityType type, Location loc, double viewingDistance) {
		super(name, type, loc);
		this.viewingDistance = viewingDistance;
	}
	
}
