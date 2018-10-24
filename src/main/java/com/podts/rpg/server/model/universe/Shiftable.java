package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Shiftable<T extends Shiftable<T>> extends HasLocation {
	
	public T shift(int dx, int dy, int dz);
	
	public default T shift(int dx, int dy) {
		return shift(dx, dy, 0);
	}
	
	public default T shift(Vector vector) {
		return shift(vector.getX(), vector.getY(), vector.getZ());
	}
	
	public default T shift(Direction d, int distance) {
		return shift(d.getX(distance), d.getY(distance));
	}
	
	public default T shift(Direction d) {
		return shift(d.getX(), d.getY());
	}
	
}
