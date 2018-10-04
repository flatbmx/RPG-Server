package com.podts.rpg.server.model.universe;

import java.util.Collection;

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
	
	@SuppressWarnings("unchecked")
	public default <S extends HasSpace> boolean isInSameSpace(S... spaces) {
		Space space = getSpace();
		for(S s : spaces) {
			if(!space.equals(s.getSpace()))
				return false;
		}
		return true;
	}
	
	public default <S extends HasSpace> boolean isInSameSpace(Collection<S> spaces) {
		Space space = getSpace();
		for(S s : spaces) {
			if(!space.equals(s.getSpace()))
				return false;
		}
		return true;
	}
	
	public default boolean isInDifferentSpace(HasSpace s) {
		return !isInSameSpace(s);
	}
	
}
