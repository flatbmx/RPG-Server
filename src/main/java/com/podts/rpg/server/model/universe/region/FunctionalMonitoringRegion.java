package com.podts.rpg.server.model.universe.region;

import java.util.function.Predicate;

import com.podts.rpg.server.model.universe.Location;

public class FunctionalMonitoringRegion extends SimpleMonitoringRegion {
	
	private final Predicate<Location> containsFunction;
	
	@Override
	public final boolean contains(Location point) {
		return containsFunction.test(point);
	}
	
	public FunctionalMonitoringRegion(Predicate<Location> containsFunction) {
		this.containsFunction = containsFunction;
	}
	
}
