package com.podts.rpg.server.model;

import com.podts.rpg.server.model.universe.World;

public interface Locatable {
	
	public Location getLocation();
	
	public default World getWorld() {
		return getLocation().getWorld();
	}
	
	public default double distance(Locatable o) {
		return getLocation().distance(o.getLocation());
	}
	
}
