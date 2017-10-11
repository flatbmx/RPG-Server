package com.podts.rpg.server.model.universe.region;

public interface DynamicRegion extends Region {
	
	public boolean changesShape();
	public boolean changesVolume();
	
}
