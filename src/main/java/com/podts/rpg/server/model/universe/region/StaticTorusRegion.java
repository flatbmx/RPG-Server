package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

class StaticTorusRegion extends SimpleRegion implements TorusRegion {
	
	private final Location center;
	private final int innerRadius, outerRadius;
	
	@Override
	public Location getCenter() {
		return center;
	}

	@Override
	public int getOuterRadius() {
		return outerRadius;
	}

	@Override
	public int getInnerRadius() {
		return innerRadius;
	}
	
	StaticTorusRegion(final Locatable center, final int outerRadius, final int innerRadius) {
		this.center = center.getLocation();
		this.outerRadius = outerRadius;
		this.innerRadius = innerRadius;
	}
	
}