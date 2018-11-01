package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public class SpiralShifter extends ShiftBehavior {
	
	private Direction dir = Direction.UP;
	private final RelationalDirection turnDir;
	private int moved, moveTotal = 1;
	private byte moveCounter;
	
	private Vector current = dir.asVector();
	
	@Override
	public boolean hasNext() {
		return true;
	}
	
	@Override
	public Vector next() {
		Vector result = current;
		++moved;
		if(moved == moveTotal) {
			dir = dir.convert(turnDir, 2);
			current = dir.asVector();
			moved = 0;
			++moveCounter;
			if(moveCounter == 2) {
				moveCounter = 0;
				++moveTotal;
			}
		}
		return result;
	}
	
	SpiralShifter(RelationalDirection dir) {
		turnDir = dir;
	}
	
	public SpiralShifter() {
		this(RelationalDirection.RIGHT);
	}
	
}
