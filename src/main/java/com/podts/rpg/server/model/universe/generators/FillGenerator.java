package com.podts.rpg.server.model.universe.generators;

import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.TileElement;
import com.podts.rpg.server.model.universe.WorldGenerator;
import com.podts.rpg.server.model.universe.TileElement.TileType;

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
	public TileElement doGenerateTile(Location point) {
		return constructElement(type);
	}
	
	public FillGenerator(TileType type) {
		this.type = type;
	}
	
}
