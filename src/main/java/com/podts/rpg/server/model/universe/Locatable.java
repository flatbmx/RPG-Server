package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Locatable extends HasPlane {
	
	public Location getLocation();
	
	public default boolean isNowhere() {
		return Space.getNowhere().equals(getLocation());
	}
	
	public default boolean isSomewhere() {
		return !isNowhere();
	}
	
	@Override
	public default Plane getPlane() {
		return getLocation().getPlane();
	}
	
	public default Tile getTile() {
		return getSpace().getTile(this);
	}
	
	public default Direction getDirectionTo(Locatable other) {
		return Direction.get(this, other);
	}
	
	public default Collection<Tile> getSurroundingTiles() {
		return getSpace().getSurroundingTiles(this);
	}
	
	public default Stream<Tile> surroundingTiles() {
		return getSpace().surroundingTiles(this);
	}
	
	public default Stream<Tile> surroundingTiles(int distance) {
		return getSpace().surroundingTiles(this, distance);
	}
	
	public default Iterable<Tile> getSurroundingTilesIterable() {
		return getSpace().getSurroundingTilesIterable(this);
	}
	
	public default double distance(Locatable o) {
		return getLocation().distance(o.getLocation());
	}
	
	public default int walkingDistance(Locatable o) {
		return getLocation().walkingDistance(o.getLocation());
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
		int dist = walkingDistance(o);
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
		return isInPlane(l.getPlane());
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
		if(a.isInDifferentSpace(b)) return false;
		return isBetweenPlanes(a.getZ(), b.getZ());
	}
}
