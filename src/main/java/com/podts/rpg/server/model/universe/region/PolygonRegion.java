package com.podts.rpg.server.model.universe.region;

import java.util.List;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location;

public interface PolygonRegion extends Region {
	
	public List<? extends Location> getCorners();
	
	public default Stream<? extends Location> corners() {
		return getCorners().stream();
	}
	
}
