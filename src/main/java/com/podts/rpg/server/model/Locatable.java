package com.podts.rpg.server.model;

public interface Locatable {
	
	public Location getLocation();
	
	public default double distance(Locatable o) {
		return getLocation().distance(o.getLocation());
	}
	
}
