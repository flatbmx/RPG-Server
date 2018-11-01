package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public interface MovableFacable<T extends MovableFacable<T>> extends Movable<T>, Facable {
	
	public default T move(RelationalDirection relDir, int distance) {
		return move(relDir.convert(getFacingDirection()), distance);
	}
	
	public default T move(RelationalDirection relDir) {
		return move(relDir, 1);
	}
	
	public default T moveForward(int distance) {
		return move(getFacingDirection(), distance);
	}
	
	public default T moveForward() {
		return moveForward(1);
	}
	
	public default T moveBackward(int distance) {
		return move(RelationalDirection.BACKWARD, distance);
	}
	
	public default T moveBackward() {
		return moveBackward(1);
	}
	
	public default T strafeLeft(int distance) {
		return move(RelationalDirection.LEFT, distance);
	}
	
	public default T strafeLeft() {
		return strafeLeft(1);
	}
	
	public default T strafeRight(int distance) {
		return move(RelationalDirection.RIGHT, distance);
	}
	
	public default T strafeRight() {
		return strafeRight(1);
	}
	
}
