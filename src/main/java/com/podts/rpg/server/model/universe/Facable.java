package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public interface Facable {
	
	public Direction getFacingDirection();
	public Facable face(Direction dir);
	
	public default Facable turn(RelationalDirection dir) {
		face(getFacingDirection().convert(dir));
		return this;
	}
	
	public default boolean isFacing(Direction dir) {
		return getFacingDirection().equals(dir);
	}
	
}
