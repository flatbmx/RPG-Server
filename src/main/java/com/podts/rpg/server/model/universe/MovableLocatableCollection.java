package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class MovableLocatableCollection extends LocatableCollection implements Movable {
	
	public static MovableLocatableCollection construct(Collection<Location> points) {
		if(!points.isEmpty()) {
			Iterator<Location> it = points.iterator();
			Plane plane = it.next().getPlane();
			while(it.hasNext()) {
				if(!plane.equals(it.next().getPlane()))
					throw new IllegalArgumentException("Cannot construct MovableLocatableColelction with points from different planes!");
			}
		}
		return new MovableLocatableCollection(points);
	}
	
	@Override
	public MovableLocatableCollection move(int dx, int dy, int dz) {
		if(isNowhere())
			return this;
		Location[] newPoints = new Location[getPoints().size()];
		int i=0;
		for(Location point : getPoints()) {
			newPoints[i++] = point.shift(dx, dy, dz);
		}
		getPoints().clear();
		getPoints().addAll(Arrays.asList(newPoints));
		return this;
	}
	
	MovableLocatableCollection(Collection<Location> points) {
		super(points);
	}
	
}
