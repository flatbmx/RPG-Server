package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Shiftable extends HasLocation {
	
	public Shiftable shift(int dx, int dy, int dz);
	
	public default Shiftable shift(int dx, int dy) {
		return shift(dx, dy, 0);
	}
	
	public default Shiftable shift(Direction d, int distance) {
		return shift(d.getX(distance), d.getY(distance));
	}
	
	public default Shiftable shift(Direction d) {
		return shift(d.getX(), d.getY());
	}
	
}
