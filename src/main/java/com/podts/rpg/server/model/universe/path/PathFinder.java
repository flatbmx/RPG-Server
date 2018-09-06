package com.podts.rpg.server.model.universe.path;

import com.podts.rpg.server.model.universe.HasLocation;

public interface PathFinder {
	
	public static final int DEFAULT_MAX_LENGTH = 30;
	public static final PathDecider DEFAULT_DECIDER = new LengthPathDecider(DEFAULT_MAX_LENGTH);
	
	public Path findPath(HasLocation start, HasLocation finish, PathDecider decider);
	
	public default Path findPath(HasLocation start, HasLocation finish, int maxLength) {
		if(start.isAt(finish))
			return new GeneralListPath(start.getTile());
		maxLength = Math.max(0, maxLength);
		if(maxLength < start.walkingDistance(finish))
			return null;
		
		return findPath(start, finish, new LengthPathDecider(maxLength));
	}
	
	public default Path findPath(HasLocation start, HasLocation finish) {
		return findPath(start, finish, DEFAULT_DECIDER);
	}
	
}
