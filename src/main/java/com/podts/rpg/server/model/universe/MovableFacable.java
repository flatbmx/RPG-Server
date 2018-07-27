package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface MovableFacable extends Movable, Facable {
	
	public default MovableFacable move(int distance) {
		Direction dir = getFacingDirection();
		move(dir.getX() * distance, dir.getY() * distance);
		return this;
	}
	
}
