package com.podts.rpg.server.model.universe;

@FunctionalInterface
public interface TileGenerator {
	
	public TileElement[][][] generate();
	
}
