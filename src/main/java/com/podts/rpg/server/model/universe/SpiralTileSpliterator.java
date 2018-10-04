package com.podts.rpg.server.model.universe;

import java.util.Spliterator;
import java.util.function.Consumer;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public class SpiralTileSpliterator implements Spliterator<Tile> {
	
	Tile start, current, end;
	private Direction dir = Direction.UP;
	private final RelationalDirection turnDir;
	private int moved, moveTotal = 1;
	private byte moveCounter;
	private long left;
	
	private long getSeq(Tile tile) {
		long seq = 0;
		Tile t = start;
		int m = 0, mt = 1, mc = 0;
		Direction d = Direction.UP;
		
		while(t.equals(tile)) {
			t = t.shift(dir);
			++seq;
			++m;
			if(m == mt) {
				d.convert(turnDir);
				m = 0;
				++mc;
				if(mc == 2) {
					mc = 0;
					++mt;
				}
			}
		}
		return seq;
	}
	
	@Override
	public boolean tryAdvance(Consumer<? super Tile> action) {
		action.accept(current);
		current = current.shift(dir);
		++moved;
		if(moved == moveTotal) {
			dir = dir.convert(turnDir);
			moved = 0;
			++moveCounter;
			if(moveCounter == 2) {
				moveCounter = 0;
				++moveTotal;
			}
		}
		--left;
		return true;
	}
	
	@Override
	public Spliterator<Tile> trySplit() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public long estimateSize() {
		return left;
	}
	
	@Override
	public int characteristics() {
		return Spliterator.ORDERED;
	}
	
	SpiralTileSpliterator(RelationalDirection dir) {
		turnDir = dir;
	}
	
	SpiralTileSpliterator() {
		this(RelationalDirection.RIGHT);
	}
	
}
