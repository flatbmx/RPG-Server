package com.podts.rpg.server.model.universe;

public abstract class SimpleSpatial implements Spatial {
	
	public static final Location validate(Location location) {
		if(location == null)
			return Space.getNowhere();
		return location;
	}
	
	private Location location;
	
	public final Location getLocation() {
		return location;
	}
	
	final Spatial setLocation(Location location) {
		this.location = location;
		return this;
	}
	
	@Override
	public final Space getSpace() {
		return getLocation().getSpace();
	}
	
	public Tile getTile() {
		return getSpace().getTile(getLocation());
	}
	
	public SimpleSpatial(Location location) {
		this.location = validate(location);
	}
	
	public SimpleSpatial() {
		this.location = Space.getNowhere();
	}
	
}
