package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class LocatableCollection implements Locatable {
	
	public static LocatableCollection construct(Collection<Location> points) {
		if(!points.isEmpty()) {
			Iterator<Location> it = points.iterator();
			Plane plane = it.next().getPlane();
			while(it.hasNext()) {
				if(!plane.equals(it.next().getPlane()))
					throw new IllegalArgumentException("Cannot construct LocatableCollection with points from different planes!");
			}
		}
		return new LocatableCollection(points);
	}
	
	protected final Collection<Location> points, safePoints;
	
	protected final Collection<Location> getPoints() {
		return points;
	}
	
	protected final Collection<Location> getSafePoints() {
		return safePoints;
	}
	
	@Override
	public final Collection<Location> getLocations() {
		return safePoints;
	}
	
	LocatableCollection(Collection<Location> points) {
		this.points = points;
		safePoints = Collections.unmodifiableCollection(points);
	}
	
}
