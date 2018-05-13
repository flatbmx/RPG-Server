package com.podts.rpg.server.model.universe.region;

import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

public interface TorusRegion extends CenteredRegion, PollableRegion {
	
	public int getOuterRadius();
	public int getInnerRadius();
	
	@Override
	public default boolean contains(Locatable loc) {
		return getCenter().isBetween(loc, getInnerRadius(), getOuterRadius());
	}
	
	@Override
	public default Set<Location> getPoints() {
		final Set<Location> points = new HashSet<Location>();
		for(int dy=-getOuterRadius(); dy <= getOuterRadius(); ++dy) {
			for(int dx=-getOuterRadius(); dx <= getOuterRadius(); ++dx) {
				final Location point = getCenter().move(dx, dy, 0);
				if(contains(point)) points.add(point);
			}
		}
		return points;
	}
	
}