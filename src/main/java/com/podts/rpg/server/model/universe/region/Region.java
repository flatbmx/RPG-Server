package com.podts.rpg.server.model.universe.region;

import java.util.Collection;

import com.podts.rpg.server.model.Locatable;

public interface Region {
	
	public Collection<RegionListener> getRegionListeners();
	public Region addRegionListener(RegionListener newRegionListener);
	public Region removeRegionListener(RegionListener regionListener);
	
	public boolean contains(Locatable point);
	
}
