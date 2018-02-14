package com.podts.rpg.server.model.universe.generators;

import java.util.Random;

import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.WorldGenerator;

public final class PseudoRandomGenerator extends WorldGenerator {
	
	private final Random r;
	private final int[] weights;
	private final TileType[] types;
	private final int totalWeight;
	
	@Override
	public Tile doGenerateTile(final Location point) {
		int choice = r.nextInt(totalWeight);
		TileType t = null;
		for(int i=0; i<weights.length; ++i) {
			choice -= weights[i];
			if(choice <= 0) {
				t = types[i];
				break;
			}
		}
		return point.getWorld().createTile(t, point);
	}
	
	public PseudoRandomGenerator(final Random r, final int[] weights, final TileType[] types) {
		this.weights = weights;
		this.types = types;
		int newTotal = 0;
		for(int i=0; i<weights.length; ++i)
			newTotal += weights[i];
		totalWeight = newTotal;
		this.r = r;
	}
	
	public PseudoRandomGenerator(final int[] weights, final TileType[] types) {
		this(new Random(), weights, types);
	}
	
}
