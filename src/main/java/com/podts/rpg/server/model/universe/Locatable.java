package com.podts.rpg.server.model.universe;

import java.util.Comparator;

public interface Locatable extends HasPlane {
	
	public Location getLocation();
	
	@Override
	public default Plane getPlane() {
		return getLocation().getPlane();
	}
	
	public default double distance(Locatable o) {
		return getLocation().distance(o.getLocation());
	}
	
	public default int walkingDistance(Locatable o) {
		return getLocation().WalkingDistance(o.getLocation());
	}
	
	public default Comparator<Locatable> getDistanceComparator() {
		return getLocation().getDistanceComparator();
	}
	
	public default Comparator<Locatable> getWalkingDistanceComparator() {
		return getLocation().getWalkingDistanceComparator();
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
	
	public default boolean isInPlane(Plane plane) {
		return getPlane().equals(plane);
	}
	
	public default boolean isInPlane(Locatable l) {
		return isInPlane(l.getLocation().getZ()) &&
				isInSameSpace(l);
	}
	
	public default boolean isBetweenPlanes(int minZ, int maxZ) {
		if(minZ > maxZ) {
			int tempZ = minZ;
			minZ = maxZ;
			maxZ = tempZ;
		}
		return getLocation().getZ() >= minZ &&
				getLocation().getZ() <= maxZ;
	}
	
	
	public default boolean isBetweenPlanes(Plane a, Plane b) {
		if(!a.isInSameSpace(b)) return false;
		return isBetweenPlanes(a.getZ(), b.getZ());
	}
}
