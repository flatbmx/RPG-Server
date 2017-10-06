package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Locatable;

public interface CircularRegion extends EllipticalRegion {
	
	public int getRadius();
	
	@Override
	public default int getXRadius() {
		return getRadius();
	}
	
	@Override
	public default int getYRadius() {
		return getRadius();
	}
	
	@Override
	public default boolean contains(final Locatable l) {
		return getCenter().distance(l) <= getRadius();
	}
	
}
