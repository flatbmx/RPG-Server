package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Location.Direction;

public abstract class LivingHumanoid extends AnimalEntity implements Walkable, Mammal, CanSee {
	
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
	
	public boolean canSee(Locatable loc) {
		if(!isInPlane(loc)) return false;
		return isInRange(loc, getViewingDistance());
	}
	
	@Override
	public LivingHumanoid walk(Direction direction) {
		move(direction);
		return this;
	}
	
	public LivingHumanoid(String name, EntityType type, Location loc, double viewingDistance) {
		super(name, type, loc);
		this.viewingDistance = viewingDistance;
	}
	
}
