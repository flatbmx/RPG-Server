package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Movable<T extends Movable<T>> extends Locatable {
	
	public T move(final int dx, final int dy, final int dz);
	
	public default T move(final int dx, final int dy) {
		return move(dx, dy, 0);
	}
	
	public default T move(Direction dir, int distance) {
		return move(dir.getX(distance), dir.getY(distance));
	}
	
	public default T move(Direction dir) {
		return move(dir, 1);
	}
	
	public default T move(Vector vector) {
		return move(vector.getX(), vector.getY(), vector.getZ());
	}
	
}
