package com.podts.rpg.server.model.universe;

import static org.junit.Assert.*;

import org.junit.Test;

import com.podts.rpg.server.model.universe.TileElement.TileType;

public class TestNowhere {
	
	@Test
	public void testNoWhereTile() {
		assertEquals(Space.OBLIVION, Space.NOWHERE_TILE.getSpace());
		assertTrue(Space.NOWHERE_TILE.isNowhere());
		assertTrue(Space.NOWHERE_TILE.locations().count() == 0);
		assertEquals(Space.NOWHERE, Space.NOWHERE_TILE.getLocation());
	}
	
	@Test
	public void testSpatial() {
		Spatial a = new Tile(TileType.VOID);
		assertTrue(a.isNowhere());
		assertEquals(Space.NOWHERE, a.getLocation());
		assertEquals(Space.OBLIVION, a.getSpace());
		assertNull(a.getPlane());
	}
	
}
