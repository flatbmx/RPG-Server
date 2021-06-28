package com.podts.rpg.server.model.universe;

import java.util.Spliterator;
import java.util.function.Consumer;

import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public class SquareSpreadingTileSpliterator implements Spliterator<Tile> {
	
	private final Tile center;
	private final int maxLayer;
	private final RelationalDirection turnDir;
	
	private Tile currentTile, currentCorner;
	private int currentShell = 0;
	private int shellSkip = 1;
	
	private int currentStretch;
	private int shellSize;
	
	private void nextTile() {
		if(currentTile.equals(currentCorner)) {
			currentTile = currentTile.shift(-shellSkip, -shellSkip);
		}
	}
	
	@Override
	public boolean tryAdvance(Consumer<? super Tile> action) {
		if(currentTile != null) {
			action.accept(currentTile);
			nextTile();
			return true;
		}
		return false;
	}

	@Override
	public Spliterator<Tile> trySplit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long estimateSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int characteristics() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private static final int getShellLength(int shell) {
		return 2 * shell + 1;
	}
	
	private static final int getShellSize(int shell) {
		if(shell == 0)
			return 1;
		return 8 * shell;
	}
	
	public SquareSpreadingTileSpliterator(Tile center, int maxRange, RelationalDirection turnDir) {
		this.center = center;
		this.maxLayer = maxRange;
		this.turnDir = turnDir;
		
		currentTile = center;
		currentCorner = center;
	}
	
}
