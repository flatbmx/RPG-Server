package com.podts.rpg.server.model.universe;

public abstract class Spatial implements HasLocation {
	
	private Location location;
	
	@Override
	public final Location getLocation() {
		return location;
	}
	
	final Spatial setLocation(Location location) {
		this.location = location;
		return this;
	}
	
	public Spatial(Location location) {
		this.location = Location.validate(location);
	}
	
	public Spatial() {
		this.location = Space.NOWHERE;
	}
	
}
