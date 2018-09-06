package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Spatial;

class StaticSetRegion extends AbstractSetRegion {
	
	<L extends Locatable> StaticSetRegion(Iterable<L> locs) {
		super(false, locs);
	}
	
	@SafeVarargs
	<S extends Spatial> StaticSetRegion(S... points) {
		super(false, points);
	}
	
}
