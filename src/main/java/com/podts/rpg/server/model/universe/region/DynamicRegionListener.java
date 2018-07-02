package com.podts.rpg.server.model.universe.region;

public interface DynamicRegionListener extends RegionListener {
	
	public default void onRegionChange(Region r) {
		
	}
	
}
