package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Spatial;

final class DynamicCircularRegion extends SimpleRegion implements CircularRegion {
	
	private Spatial center;
	private int radius;
	
	@Override
	public final Location getCenter() {
		return center.getLocation();
	}
	
	public final DynamicCircularRegion setCenter(final Spatial newCenter) {
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
	
	protected DynamicCircularRegion(final Spatial center, final int radius) {
		this.center = center;
		this.radius = Math.abs(radius);
	}
	
}
