package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

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
	
	StaticCircularRegion(final Locatable center, int radius) {
		this.center = center.getLocation();
		this.radius = radius;
	}
	
}
