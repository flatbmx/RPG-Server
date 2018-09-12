package com.podts.rpg.server.model.universe;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.StaticChunkWorld.CLocation;
import com.podts.rpg.server.model.universe.TileElement.TileType;
import com.podts.rpg.server.model.universe.generators.FillGenerator;

public class TestLocation {
	
	@Test
	public void testTrace() {
		
		Space space = new StaticChunkWorld("Earth", new FillGenerator(TileType.GRASS));
		
		Location point = (Location) space.getOrigin();
		Tile tile = point.getTile();
		//tile.surroundingTiles(1000).forEach(t -> {});
		
		boolean b = tile.traceEvery(Direction.UP, 6)
				.limit(10000)
				.flatMap(t -> t.traceEvery(Direction.UP, 6).limit(100))
				.map(Tile::getLocation)
				.map(CLocation.class::cast)
				.allMatch(p -> p.hasChunk() && p.getChunk().isGenerated());
		
		assertTrue(b);
		
	}
	
}
