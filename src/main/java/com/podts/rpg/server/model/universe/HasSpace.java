package com.podts.rpg.server.model.universe;

public interface HasSpace {
	
	public Space getSpace();
	
	public default boolean isInSpace(Space s) {
		return getSpace().equals(s);
	}
	
	public default boolean isInSameSpace(HasSpace s) {
		if(s == null)
			return false;
		return isInSpace(s.getSpace());
	}
	
	public default boolean isInDifferentSpace(HasSpace s) {
		return !isInSameSpace(s);
	}
	
}
