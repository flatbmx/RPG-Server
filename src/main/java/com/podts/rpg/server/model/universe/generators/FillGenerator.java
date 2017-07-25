package com.podts.rpg.server.model.universe.generators;

import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.model.universe.WorldGenerator;
import com.podts.rpg.server.model.universe.Tile.TileType;

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
	public Tile generateTile(int x, int y, int z) {
		return new Tile(type);
	}
	
	public FillGenerator(TileType type) {
		this.type = type;
	}
	
}
