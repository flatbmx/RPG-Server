package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.HasLocation;

class StaticSetRegion extends AbstractSetRegion {
	
	<L extends Locatable> StaticSetRegion(Iterable<L> locs) {
		super(false, locs);
	}
	
	@SafeVarargs
	<S extends HasLocation> StaticSetRegion(S... points) {
		super(false, points);
	}
	
}
