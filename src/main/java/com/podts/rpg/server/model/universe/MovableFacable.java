package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public interface MovableFacable extends Movable, Facable {
	
	public default MovableFacable move(RelationalDirection relDir, int distance) {
		Direction dir = relDir.convert(getFacingDirection());
		move(dir, distance);
		return this;
	}
	
	public default MovableFacable move(RelationalDirection relDir) {
		return move(relDir, 1);
	}
	
	public default MovableFacable moveForward(int distance) {
		move(getFacingDirection(), distance);
		return this;
	}
	
	public default MovableFacable moveForward() {
		return moveForward(1);
	}
	
	public default MovableFacable moveBackward(int distance) {
		return move(RelationalDirection.BACKWARD, distance);
	}
	
	public default MovableFacable moveBackward() {
		return moveBackward(1);
	}
	
	public default MovableFacable strafeLeft(int distance) {
		return move(RelationalDirection.LEFT, distance);
	}
	
	public default MovableFacable strafeLeft() {
		return strafeLeft(1);
	}
	
	public default MovableFacable strafeRight(int distance) {
		return move(RelationalDirection.RIGHT, distance);
	}
	
	public default MovableFacable strafeRight() {
		return strafeRight(1);
	}
	
}
