package com.podts.rpg.server.model.universe.region;

import java.util.function.Predicate;

import com.podts.rpg.server.model.universe.Locatable;

public class FunctionalMonitoringRegion extends SimpleMonitoringRegion {
	
	private final Predicate<Locatable> containsFunction;
	
	@Override
	public final boolean contains(Locatable point) {
		return containsFunction.test(point);
	}
	
	public FunctionalMonitoringRegion(Predicate<Locatable> containsFunction) {
		this.containsFunction = containsFunction;
	}
	
}
