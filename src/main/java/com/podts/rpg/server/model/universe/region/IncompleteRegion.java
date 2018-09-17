package com.podts.rpg.server.model.universe.region;

import java.util.Collection;

public abstract class IncompleteRegion implements Region {
	
	@Override
	public final Collection<RegionListener> getRegionListeners() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final Region addRegionListeners(RegionListener... newRegionListener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final Region removeRegionListeners(RegionListener... regionListener) {
		throw new UnsupportedOperationException();
	}

}
