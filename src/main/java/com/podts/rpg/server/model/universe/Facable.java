package com.podts.rpg.server.model.universe;

import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Facable extends Locatable {
	
	public static boolean bothFacing(Facable a, Facable b) {
		return a.isFacing(b) && b.isFacing(a);
	}
	
	public Direction getFacingDirection();
	public Facable face(Direction dir);
	
	public default Facable face(Locatable loc) {
		Direction dir = Direction.get(this, loc);
		if(dir != null)
			face(dir);
		return this;
	}
	
	public default Stream<Location> traceFacingLocations() {
		return getLocation().trace(getFacingDirection());
	}
	
	public default Stream<Tile> traceFacingTiles() {
		return getTile().trace(getFacingDirection());
	}
	
	public default boolean isFacing(Locatable loc, double distance) {
		return isInRange(loc, distance) && isFacing(loc);
	}
	
	public default boolean isFacing(Locatable loc, int distance) {
		return isInWalkingRange(loc, distance) && isFacing(loc);
	}
	
	public default boolean isFacing(Locatable loc) {
		return getFacingDirection().equals(Direction.get(this, loc));
	}
	
}
