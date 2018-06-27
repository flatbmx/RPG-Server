package com.podts.rpg.server.model.universe.region;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;
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
	
	public default Stream<Tile> tiles() {
		return points()
				.map(point -> point.getWorld().getTile(point));
	}
	
	public default Stream<Entity> entities() {
		return points()
				.flatMap(point -> point.getWorld().entities(point));
	}
	
	public default Stream<Player> players() {
		return entities()
				.filter(Player::is)
				.map(e -> ((PlayerEntity)e).getPlayer());
	}
	
	@Override
	public default Iterator<Location> iterator() {
		return getPoints().iterator();
	}
	
}
