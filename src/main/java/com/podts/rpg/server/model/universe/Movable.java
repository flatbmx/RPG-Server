package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Movable extends Locatable {
	
	public Movable move(final int dx, final int dy, final int dz);
	
	public default Movable move(final int dx, final int dy) {
		return move(dx, dy, 0);
	}
	
	public default Movable move(Direction dir, int distance) {
		return move(dir.getX() * distance, dir.getY() * distance);
	}
	
	public default Movable move(Direction dir) {
		return move(dir, 1);
	}
	
}
