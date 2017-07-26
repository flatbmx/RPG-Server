package com.podts.rpg.server.model.universe.region;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A basic implementation for managing {@link RegionListener}s using a {@link HashSet}.
 * This does not implement {@link #contains(Locatable)} and therefore leaves that up to it's sub-classes.
 *
 */
public abstract class SimpleRegionHandler implements Region {
	
	private final Set<RegionListener> regionListeners = new HashSet<RegionListener>();
	private final Set<RegionListener> safeRegionListeners = Collections.unmodifiableSet(regionListeners);
	
	@Override
	public Set<RegionListener> getRegionListeners() {
		return safeRegionListeners;
	}

	@Override
	public Region addRegionListener(RegionListener newRegionListener) {
		regionListeners.add(newRegionListener);
		return this;
	}

	@Override
	public Region removeRegionListener(RegionListener regionListener) {
		regionListeners.remove(regionListener);
		return this;
	}
	
}
