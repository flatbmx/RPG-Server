package com.podts.rpg.server.model.universe.region;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class ConcerningRegion implements Region {
	
	private Set<RegionListener> regionListeners, safeRegionListeners;
	
	@Override
	public Collection<RegionListener> getRegionListeners() {
		if(safeRegionListeners == null)
			return Collections.emptyList();
		return safeRegionListeners;
	}
	
	@Override
	public ConcerningRegion addRegionListeners(RegionListener... listeners) {
		checkCreate();
		for(RegionListener l : listeners)
			regionListeners.add(l);
		return this;
	}
	
	@Override
	public ConcerningRegion removeRegionListeners(RegionListener... listeners) {
		for(RegionListener l : listeners)
			regionListeners.remove(l);
		checkDelete();
		return this;
	}
	
	private final void checkCreate() {
		if(regionListeners == null) {
			regionListeners = new HashSet<>();
			safeRegionListeners = Collections.unmodifiableSet(regionListeners);
		}
	}
	
	private final void checkDelete() {
		if(regionListeners.isEmpty()) {
			regionListeners = null;
			safeRegionListeners = null;
		}
	}
	
}
