package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Movable extends Locatable {
	
	public Movable move(int dx, int dy, int dz);
	
	public default Movable move(final int dx, final int dy) {
		return move(dx, dy, 0);
	}
	
	public default Movable move(Direction dir) {
		return move(dir.getX(), dir.getY());
	}
	
}
