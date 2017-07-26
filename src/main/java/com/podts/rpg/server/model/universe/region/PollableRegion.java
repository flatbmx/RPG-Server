package com.podts.rpg.server.model.universe.region;

import java.util.Collection;

import com.podts.rpg.server.model.universe.Location;

/**
 * A Region that can be polled to give all the points that it contains at any give time.
 *
 */
public interface PollableRegion extends Region {
	
	/**
	 * Returns all the points that represents this Region.
	 * @return Un-Modifiable Collection of all the points.
	 */
	public Collection<Location> getPoints();
	
}
