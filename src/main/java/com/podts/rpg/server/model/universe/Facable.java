package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Facable extends Locatable {
	
	public Direction getFacingDirection();
	public Facable face(Direction dir);
	
}
