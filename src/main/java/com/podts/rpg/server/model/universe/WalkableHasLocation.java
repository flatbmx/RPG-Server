package com.podts.rpg.server.model.universe;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface WalkableHasLocation extends Walkable, HasLocation {
	
	public default List<? extends Location> getWalkingLocations() {
		List<Direction> steps = getWalkingQueue();
		if(steps.isEmpty())
			return Collections.emptyList();
		List<Location> result = new LinkedList<>();
		Iterator<Direction> it = steps.iterator();
		Location previous = getLocation();
		while(it.hasNext()) {
			result.add(previous.shift(it.next()));
		}
		return result; 
	}
	
	public default Stream<? extends Location> walkingLocations() {
		return getWalkingLocations().stream();
	}
	
	@Override
	public default List<? extends Locatable> getWalkingLocatables() {
		List<Direction> steps = getWalkingQueue();
		if(steps.isEmpty())
			return Collections.emptyList();
		List<Locatable> result = new LinkedList<>();
		Iterator<Direction> it = steps.iterator();
		Location previous = getLocation();
		while(it.hasNext()) {
			result.add(previous.shift(it.next()));
		}
		return result; 
	}
	
}
