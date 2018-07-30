package com.podts.rpg.server.model.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.RelationalDirection;

class TestDirections {
	
	@Test
	void testRelationalDirections() {
		
		for(Direction d : Direction.values())
			assertEquals(d, d.convert(RelationalDirection.FORWARD));
		
		//Backward
		assertEquals(Direction.DOWN, Direction.UP.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.UP, Direction.DOWN.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.RIGHT, Direction.LEFT.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.LEFT, Direction.RIGHT.convert(RelationalDirection.BACKWARD));
		
		//Left
		assertEquals(Direction.LEFT, Direction.UP.convert(RelationalDirection.LEFT));
		assertEquals(Direction.DOWN, Direction.LEFT.convert(RelationalDirection.LEFT));
		assertEquals(Direction.RIGHT, Direction.DOWN.convert(RelationalDirection.LEFT));
		assertEquals(Direction.UP, Direction.RIGHT.convert(RelationalDirection.LEFT));
		
		//Right
		assertEquals(Direction.RIGHT, Direction.UP.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.LEFT, Direction.DOWN.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.UP, Direction.LEFT.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.DOWN, Direction.RIGHT.convert(RelationalDirection.RIGHT));
		
	}
	
}
