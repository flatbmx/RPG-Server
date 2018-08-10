package com.podts.rpg.server.model.universe;

import org.junit.jupiter.api.Test;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.StaticChunkWorld.CLocation;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.generators.FillGenerator;

class TestLocation {
	
	@Test
	void testTrace() {
		
		Space space = new StaticChunkWorld("Earth", new FillGenerator(TileType.GRASS));
		
		CLocation point = (CLocation) space.getOrigin();
		Tile tile = point.getTile();
		tile.surroundingTiles(1000).forEach(t -> t.getSpace());
		
		boolean b = tile.traceEvery(Direction.UP, 3)
		.limit(10000)
		.map(Tile::getLocation)
		.map(CLocation.class::cast)
		.allMatch(p -> p.hasChunk() && p.getChunk().isGenerated());
		
		System.out.println(b);
		
	}
	
}
