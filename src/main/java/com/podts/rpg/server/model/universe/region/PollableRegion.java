package com.podts.rpg.server.model.universe.region;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Plane;
import com.podts.rpg.server.model.universe.Space;
import com.podts.rpg.server.model.universe.Tile;

/**
 * A Region that can be polled to give all the points that it contains at any give time.
 *
 */
public interface PollableRegion extends Region, Iterable<Location> {
	
	/**
	 * Returns all the points that represents this Region.
	 * @return Un-Modifiable Collection of all the points.
	 */
	public Collection<Location> getPoints();
	
	public default Stream<Location> points() {
		return getPoints().stream();
	}	
	
	@Override
	public default boolean contains(Location l) {
		return points()
				.anyMatch(l::isAt);
	}
	
	public default Stream<? extends Space> spaces() {
		return points()
				.map(Location::getSpace)
				.distinct();
	}
	
	public default Stream<? extends Plane> planes() {
		return points()
				.map(Location::getPlane)
				.distinct();
	}
	
	public default Stream<? extends Tile> tiles() {
		return points()
				.map(Location::getTile);
	}
	
	public default Collection<? extends Tile> getTiles() {
		return tiles()
				.collect(Collectors.toSet());
	}
	
	public default Stream<Entity> entities() {
		return planes()
				.flatMap(Plane::entities)
				.filter(this::contains);
	}
	
	public default Stream<Player> players() {
		return entities()
				.filter(Player::is)
				.map(Player::get);
	}
	
	@Override
	public default Iterator<Location> iterator() {
		return getPoints().iterator();
	}
	
}
