package com.podts.rpg.server.model.universe.path;

import java.util.Optional;

import com.podts.rpg.server.model.universe.HasLocation;

public interface PathFinder {
	
	public static final int DEFAULT_MAX_LENGTH = 30;
	public static final PathDecider DEFAULT_DECIDER = new LengthPathDecider(DEFAULT_MAX_LENGTH);
	
	public Optional<Path> findPath(HasLocation start, HasLocation finish, PathDecider decider);
	
	public default Optional<Path> findPath(HasLocation start, HasLocation finish, int maxLength) {
		if(start.isAt(finish))
			return Optional.of(new GeneralListPath(start.getTile()));
		maxLength = Math.max(0, maxLength);
		if(maxLength < start.distance(finish))
			return Optional.empty();
		
		return findPath(start, finish, new LengthPathDecider(maxLength));
	}
	
	public default Optional<Path> findPath(HasLocation start, HasLocation finish) {
		return findPath(start, finish, DEFAULT_DECIDER);
	}
	
}
