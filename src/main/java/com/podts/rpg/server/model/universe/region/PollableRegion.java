package com.podts.rpg.server.model.universe.region;

import java.util.Iterator;
import java.util.Set;

import com.podts.rpg.server.model.universe.Location;

/**
 * A Region that can be polled to give all the points that it contains at any give time.
 *
 */
public interface PollableRegion extends Region, Iterable<Location> {
	
	/**
	 * Returns all the points that represents this Region.
	 * @return Un-Modifiable Collection of all the points.
	 */
	public Set<Location> getPoints();
	
	@Override
	public default Iterator<Location> iterator() {
		return getPoints().iterator();
	}
	
}
