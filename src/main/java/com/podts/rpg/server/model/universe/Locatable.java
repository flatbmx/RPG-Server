package com.podts.rpg.server.model.universe;

public interface Locatable extends HasSpace {
	
	public Location getLocation();
	
	@Override
	public default Space getSpace() {
		return getLocation().getSpace();
	}
	
	public default double distance(Locatable o) {
		return getLocation().distance(o.getLocation());
	}
	
	public default int walkingDistance(Locatable o) {
		return getLocation().WalkingDistance(o.getLocation());
	}
	
	public default boolean isInRange(Locatable o, double distance) {
		return distance(o) <= distance;
	}
	
	public default boolean isInWalkingRange(Locatable o, int distance) {
		return walkingDistance(o) <= distance;
	}
	
	public default boolean isBetween(Locatable o, double min, double max) {
		double dist = distance(o);
		return dist >= min && dist <= max;
	}
	
	public default boolean isWalkingBetween(Locatable o, int min, int max) {
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
	
	public default boolean isBetweenPlanes(int minZ, int maxZ) {
		return getLocation().getZ() >= minZ &&
				getLocation().getZ() <= maxZ;
	}
	
}
