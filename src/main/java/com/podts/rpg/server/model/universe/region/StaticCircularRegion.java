package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Spatial;

class StaticCircularRegion extends SimpleRegion implements CircularRegion {
	
	private final Location center;
	private final int radius;
	
	public final Location getCenter() {
		return center;
	}
	
	@Override
	public final int getRadius() {
		return radius;
	}
	
	StaticCircularRegion(final Spatial center, int radius) {
		this.center = center.getLocation();
		this.radius = radius;
	}
	
}
