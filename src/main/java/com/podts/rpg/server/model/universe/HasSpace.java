package com.podts.rpg.server.model.universe;

import java.util.Collection;

/**
 * Something has has or inside exactly one {@link Space}.
 * This interface only requires that {@link #getSpace()} be implemented.
 * @author David
 *
 */
public interface HasSpace {
	
	/**
	 * Returns the {@link Space space} that this occupies or has.
	 * This method is <b>guarenteed</b> to return a non-null value.
	 * If you wish to represent this object has having no space then this method should return {@link Space#OBLIVION}.
	 * @return the {@link Space space} that this occupies or has.
	 */
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
