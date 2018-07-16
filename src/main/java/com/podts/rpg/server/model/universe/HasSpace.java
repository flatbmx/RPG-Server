package com.podts.rpg.server.model.universe;

public interface HasSpace {
	
	public Space getSpace();
	
	public default boolean isInSameSpace(HasSpace s) {
		return getSpace().equals(s.getSpace());
	}
	
}
