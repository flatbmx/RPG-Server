package com.podts.rpg.server.model.universe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Walkable extends MovableFacable {
	
	public List<Direction> getWalkingQueue();
	
	public default Stream<Direction> walkingSteps() {
		return getWalkingQueue().stream();
	}
	
	public default List<? extends Locatable> getWalkingLocatables() {
		List<Direction> steps = getWalkingQueue();
		if(steps.isEmpty())
			return Collections.emptyList();
		Collection<Location> previousPoints = getLocations();
		Iterator<Direction> it = steps.iterator();
		List<Locatable> result = new ArrayList<>();
		while(it.hasNext()) {
			Direction dir = it.next();
			Collection<Location> points = previousPoints.stream()
					.map(p -> p.shift(dir))
					.collect(Collectors.toSet());
			result.add(new LocatableCollection(points));
			previousPoints = points;
		}
		return result;
	}
	
	public default Stream<? extends Locatable> walkingLocatables() {
		return getWalkingLocatables().stream();
	}
	
	public default boolean isWalking() {
		return !getWalkingQueue().isEmpty();
	}
	
	public default boolean willWalkOn(Locatable loc) {
		if(!isWalking())
			return false;
		return walkingLocatables()
				.anyMatch(loc::isAt);
	}
	
	public Walkable addStep(Direction direction);
	
	public default Walkable addSteps(Direction... directions) {
		for(Direction dir : directions)
			addStep(dir);
		return this;
	}
	
	public Walkable stopWalking();
	
	public default Walkable walk(Direction direction) {
		if(!isWalking())
			addStep(direction);
		return this;
	}
	
}
