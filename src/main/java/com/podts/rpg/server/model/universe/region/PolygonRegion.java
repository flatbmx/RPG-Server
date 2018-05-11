package com.podts.rpg.server.model.universe.region;

import java.util.List;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location;

public interface PolygonRegion extends Region {
	
	public List<Location> getCorners();
	
	public default Stream<Location> corners() {
		return getCorners().stream();
	}
	
}
