package com.podts.rpg.server.model.universe;

public abstract class Spatial implements HasLocation {
	
	public static final Location validate(Location location) {
		if(location == null)
			return Space.getNowhere();
		return location;
	}
	
	private Location location;
	
	public final Location getLocation() {
		return location;
	}
	
	final HasLocation setLocation(Location location) {
		this.location = location;
		return this;
	}
	
	public Spatial(Location location) {
		this.location = validate(location);
	}
	
	public Spatial() {
		this.location = Space.getNowhere();
	}
	
}
