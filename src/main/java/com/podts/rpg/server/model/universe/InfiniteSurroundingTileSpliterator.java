package com.podts.rpg.server.model.universe;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public class InfiniteSurroundingTileSpliterator implements Spliterator<Tile> {
	
	public static final Stream<Tile> surroundingTiles(Tile tile, RelationalDirection dir) {
		if(tile == null)
			return Stream.empty();
		if(dir == null || !dir.turns())
			dir = DEFAULT_TURN;
		return StreamSupport.stream(new InfiniteSurroundingTileSpliterator(tile, dir), false);
	}
	
	public static final Stream<Tile> surroundingTiles(Tile tile) {
		if(tile == null)
			return Stream.empty();
		return StreamSupport.stream(new InfiniteSurroundingTileSpliterator(tile, DEFAULT_TURN), false);
	}
	
	public static final Stream<Tile> surroundingTiles(Tile tile, int radius) {
		//TODO implement limiter in spliterator so it can split for parallel performance.
		return surroundingTiles(tile)
				.limit((long)Math.pow(radius * 2 + 1, 2));
	}
	
	public static final RelationalDirection DEFAULT_TURN = RelationalDirection.RIGHT;
	
	private Tile current;
	private Direction dir = Direction.UP;
	private final RelationalDirection turnDir;
	private int moved, strideLength = 1;
	private byte moveCounter;
	
	@Override
	public boolean tryAdvance(Consumer<? super Tile> action) {
		action.accept(current);
		current = current.shift(dir);
		++moved;
		if(moved == strideLength) {
			turn();
			moved = 0;
			++moveCounter;
			if(moveCounter == 2) {
				moveCounter = 0;
				++strideLength;
			}
		}
		return true;
	}
	
	@Override
	public Spliterator<Tile> trySplit() {
		return null;
	}
	
	@Override
	public long estimateSize() {
		return Long.MAX_VALUE;
	}
	
	@Override
	public int characteristics() {
		return Spliterator.ORDERED;
	}
	
	private void turn() {
		dir = dir.convert(turnDir);
	}
	
	InfiniteSurroundingTileSpliterator(Tile origin, RelationalDirection turnDir) {
		current = origin;
		this.turnDir = turnDir;
	}
	
}
