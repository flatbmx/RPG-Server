package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Locatable;

class StaticSetRegion extends AbstractSetRegion {
	
	<L extends Locatable> StaticSetRegion(Iterable<L> locs) {
		super(false, locs);
	}
	
	@SafeVarargs
	<L extends Locatable> StaticSetRegion(L... points) {
		super(false, points);
	}
	
}
