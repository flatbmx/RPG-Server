package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * A Locatable that occupies exactly one position or nowhere.
 * @author David
 *
 */
public interface HasLocation extends Locatable {
	
	/**
	 * Returns the location that this occupies.
	 * If this is nowhere it will return {@link Space#NOWHERE}.
	 * This is <b>guaranteed</b> to return a non-null value.
	 * @return the location this occupies.
	 */
	public Location getLocation();
	
	public default Tile getTile() {
		return getLocation().getTile();
	}
	
	@Override
	public default boolean isNowhere() {
		return getLocation().isNowhere();
	}
	
	@Override
	public default Collection<Location> getLocations() {
		if(isNowhere())
			return Collections.emptySet();
		return Collections.singleton(getLocation());
	}
	
	@Override
	public default Stream<Location> locations() {
		if(isNowhere())
			return Stream.empty();
		return Stream.of(getLocation());
	}
	
	@Override
	public default boolean occupies(HasLocation loc) {
		return isAt(loc);
	}
	
	@Override
	public default Space getSpace() {
		return getLocation().getSpace();
	}
	
	@Override
	public default Plane getPlane() {
		return getLocation().getPlane();
	}
	
	public default boolean isAt(HasLocation loc) {
		return getLocation().equals(loc.getLocation());
	}
	
	@Override
	public default boolean isAt(Locatable loc) {
		return loc.locations()
				.anyMatch(this::isAt);
	}
	
	@Override
	public default int getArea() {
		if(isNowhere())
			return 0;
		return 1;
	}
	
	public default Stream<Tile> surroundingTiles(int radius) {
		return getSpace().doSurroundingTiles(this, radius);
	}
	
	public default Stream<Tile> surroundTiles() {
		return surroundingTiles(1);
	}
	
}