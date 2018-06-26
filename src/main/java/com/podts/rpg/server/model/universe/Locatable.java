package com.podts.rpg.server.model.universe;

public interface Locatable {
	
	public Location getLocation();
	
	public default World getWorld() {
		return getLocation().getWorld();
	}
	
	public default double distance(Locatable o) {
		return getLocation().distance(o.getLocation());
	}
	
	public default boolean isInRange(Locatable o, double distance) {
		return distance(o) <= distance;
	}
	
	public default boolean isBetween(Locatable o, double min, double max) {
		double dist = distance(o);
		return dist >= min && dist <= max;
	}
	
	public default boolean isAt(Locatable point) {
		return getLocation().equals(point.getLocation());
	}
	
	public default boolean isInPlane(int z) {
		return getLocation().getZ() == z;
	}
	
	public default boolean isInPlane(Locatable l) {
		return isInPlane(l.getLocation().getZ());
	}
	
}
