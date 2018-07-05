package com.podts.rpg.server.model.universe.generators;

import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.WorldGenerator;

/**
 * World Generator that generates the same Tile everywhere.
 *
 */
public final class FillGenerator extends WorldGenerator {
	
	private final TileType type;
	
	public final TileType getTileType() {
		return type;
	}
	
	@Override
	public Tile doGenerateTile(Location point) {
		return point.getSpace().createTile(type, point);
	}
	
	public FillGenerator(TileType type) {
		this.type = type;
	}
	
}
