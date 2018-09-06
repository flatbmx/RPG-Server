package com.podts.rpg.server.model.universe.region;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.HasLocation;

public abstract class AbstractSetRegion extends SimpleRegion implements SetRegion {
	
	Set<Location> points;
	Set<Location> safePoints;
	
	@Override
	public final Set<Location> getPoints() {
		return safePoints;
	}

	@Override
	public final boolean contains(Location point) {
		return getPoints().contains(point);
	}

	@Override
	public final SetRegion addPoint(HasLocation point) {
		points.add(point.getLocation());
		return this;
	}

	@Override
	public final SetRegion removePoint(HasLocation point) {
		points.remove(point.getLocation());
		return this;
	}
	
	AbstractSetRegion() {
		this(new HashSet<Location>());
	}
	
	<L extends Locatable> AbstractSetRegion(boolean dynamic, Iterable<L> locs) {
		Set<Location> pSet = new HashSet<>();
		for(Locatable loc : locs) {
			pSet.addAll(loc.getLocations());
		}
		safePoints = Collections.unmodifiableSet(pSet);
		if(dynamic) {
			this.points = pSet;
			
		} else {
			this.points = safePoints;
		}
	}
	
	@SafeVarargs
	<S extends HasLocation> AbstractSetRegion(boolean dynamic, S... points) {
		Set<Location> pSet = new HashSet<>();
		for(S loc : points) {
			pSet.add(loc.getLocation());
		}
		safePoints = Collections.unmodifiableSet(pSet);
		if(dynamic) {
			this.points = pSet;
			
		} else {
			this.points = safePoints;
		}
	}
	
	AbstractSetRegion(Set<Location> points) {
		this(points, Collections.unmodifiableSet(points));
	}
	
	AbstractSetRegion(Set<Location> points, Set<Location> safePoints) {
		this.points = points;
		this.safePoints = safePoints;
	}
	
}
