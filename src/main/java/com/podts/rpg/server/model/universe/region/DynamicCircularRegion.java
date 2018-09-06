package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.HasLocation;

final class DynamicCircularRegion extends SimpleRegion implements CircularRegion {
	
	private HasLocation center;
	private int radius;
	
	@Override
	public final Location getCenter() {
		return center.getLocation();
	}
	
	public final DynamicCircularRegion setCenter(final HasLocation newCenter) {
		center = newCenter;
		return this;
	}
	
	@Override
	public final int getRadius() {
		return radius;
	}
	
	protected final DynamicCircularRegion setRadius(final int newRadius) {
		radius = Math.abs(newRadius);
		return this;
	}
	
	protected DynamicCircularRegion(final HasLocation center, final int radius) {
		this.center = center;
		this.radius = Math.abs(radius);
	}
	
}
