package com.podts.rpg.server.model.universe.region;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.model.universe.Locatable;

/**
 * A basic implementation for managing {@link RegionListener region listeners} using a {@link HashSet}.
 * This does not implement {@link #contains(Locatable) contains} and therefore leaves that up to it's sub-classes.
 *
 */
public abstract class SimpleRegion implements Region {
	
	private final Set<RegionListener> regionListeners = new HashSet<RegionListener>();
	private final Set<RegionListener> safeRegionListeners = Collections.unmodifiableSet(regionListeners);
	
	@Override
	public final Collection<RegionListener> getRegionListeners() {
		return safeRegionListeners;
	}

	@Override
	public final SimpleRegion addRegionListeners(RegionListener... listeners) {
		for(RegionListener l : listeners)
			regionListeners.add(l);
		return this;
	}

	@Override
	public final Region removeRegionListeners(RegionListener... listeners) {
		for(RegionListener l : listeners)
			regionListeners.remove(l);
		return this;
	}
	
}
