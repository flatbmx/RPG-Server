package com.podts.rpg.server.model.universe.region;

import java.util.Collection;

public abstract class IncompleteRegion implements Region {

	@Override
	public Collection<RegionListener> getRegionListeners() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Region addRegionListener(RegionListener newRegionListener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Region removeRegionListener(RegionListener regionListener) {
		throw new UnsupportedOperationException();
	}

}
