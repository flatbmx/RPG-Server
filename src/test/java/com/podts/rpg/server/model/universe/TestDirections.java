package com.podts.rpg.server.model.universe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public class TestDirections {
	
	@Test
	public void testRelationalDirections() {
		
		for(Direction d : Direction.values())
			assertEquals(d, d.convert(RelationalDirection.FORWARD));
		
		//Backward
		assertEquals(Direction.DOWN, Direction.UP.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.UP, Direction.DOWN.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.RIGHT, Direction.LEFT.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.LEFT, Direction.RIGHT.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.BOTTOM_RIGHT, Direction.TOP_LEFT.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.BOTTOM_LEFT, Direction.TOP_RIGHT.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.TOP_LEFT, Direction.BOTTOM_RIGHT.convert(RelationalDirection.BACKWARD));
		assertEquals(Direction.TOP_RIGHT, Direction.BOTTOM_LEFT.convert(RelationalDirection.BACKWARD));
		
		//Left
		assertEquals(Direction.TOP_LEFT, Direction.UP.convert(RelationalDirection.LEFT));
		assertEquals(Direction.LEFT, Direction.TOP_LEFT.convert(RelationalDirection.LEFT));
		assertEquals(Direction.BOTTOM_LEFT, Direction.LEFT.convert(RelationalDirection.LEFT));
		assertEquals(Direction.DOWN, Direction.BOTTOM_LEFT.convert(RelationalDirection.LEFT));
		assertEquals(Direction.BOTTOM_RIGHT, Direction.DOWN.convert(RelationalDirection.LEFT));
		assertEquals(Direction.RIGHT, Direction.BOTTOM_RIGHT.convert(RelationalDirection.LEFT));
		assertEquals(Direction.TOP_RIGHT, Direction.RIGHT.convert(RelationalDirection.LEFT));
		
		//Right
		assertEquals(Direction.TOP_RIGHT, Direction.UP.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.RIGHT, Direction.TOP_RIGHT.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.BOTTOM_RIGHT, Direction.RIGHT.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.DOWN, Direction.BOTTOM_RIGHT.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.BOTTOM_LEFT, Direction.DOWN.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.LEFT, Direction.BOTTOM_LEFT.convert(RelationalDirection.RIGHT));
		assertEquals(Direction.TOP_LEFT, Direction.LEFT.convert(RelationalDirection.RIGHT));
		
	}
	
}
