package com.podts.rpg.server.model.universe.region;

import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

public interface EllipticalRegion extends CenteredRegion, PollableRegion {
	
	public int getXRadius();
	public int getYRadius();
	
	@Override
	public default boolean contains(final Locatable loc) {
		return Math.pow((getCenter().getX() - loc.getLocation().getX())/getXRadius(), 2) + Math.pow((getCenter().getY() - loc.getLocation().getY())/getYRadius(), 2) <= 1;
	}
	
	@Override
	public default Set<Location> getPoints() {
		final Set<Location> result = new HashSet<Location>();
		final Location center = getCenter();
		for( int dy = -getYRadius(); dy <= getYRadius(); ++dy) {
			for( int dx = -getXRadius(); dx <= getXRadius(); ++dx) {
				final Location point = center.shift(dx, dy, 0);
				if(contains(point))
					result.add(point);
			}
		}
		return result;
	}
	
}