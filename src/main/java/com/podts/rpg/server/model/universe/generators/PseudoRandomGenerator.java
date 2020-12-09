package com.podts.rpg.server.model.universe.generators;

import java.util.Random;

import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.TileElement;
import com.podts.rpg.server.model.universe.WorldGenerator;
import com.podts.rpg.server.model.universe.TileElement.TileType;

public final class PseudoRandomGenerator extends WorldGenerator {
	
	private final Random r;
	private final int[] weights;
	private final TileType[] types;
	private final int totalWeight;
	
	@Override
	public TileElement doGenerateTile(final Location point) {
		int choice = r.nextInt(totalWeight);
		TileType type = null;
		for(int i=0; i<weights.length; ++i) {
			choice -= weights[i];
			if(choice <= 0) {
				type = types[i];
				break;
			}
		}
		return constructElement(type);
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
