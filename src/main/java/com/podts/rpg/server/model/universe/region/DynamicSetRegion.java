package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

public class DynamicSetRegion extends AbstractSetRegion implements DynamicRegion {
	
	@Override
	public boolean changesShape() {
		return true;
	}

	@Override
	public boolean changesVolume() {
		return true;
	}
	
	DynamicSetRegion() {}
	
	<L extends Locatable> DynamicSetRegion(Iterable<L> locs) {
		super(true, locs);
	}
	
}
