package com.podts.rpg.server.model.universe.path;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.podts.rpg.server.model.universe.Tile;

public class ReferencePath extends Path {
	
	private final static class ReverseTileIterator implements Iterator<Tile> {
		
		private ReferencePath path;
		
		@Override
		public boolean hasNext() {
			return path != null;
		}

		@Override
		public Tile next() {
			Tile result = path.getEnd();
			path = path.reference;
			return result;
		}
		
		private ReverseTileIterator(ReferencePath path) {
			this.path = path;
		}
		
	}
	
	private final ReferencePath reference;
	private final int length;
	private final int turns;
	private final Tile last;
	
	@Override
	public LinkedList<Tile> getTiles() {
		LinkedList<Tile> list = new LinkedList<>();
		ReferencePath path = this;
		while(path != null) {
			list.addFirst(path.getEnd());
			path = path.reference;
		}
		return list;
	}
	
	@Override
	public int length() {
		return length;
	}
	
	@Override
	public int getTurns() {
		return turns;
	}
	
	public Stream<Tile> reverseTiles() {
		return Streams.stream(new ReverseTileIterator(this));
	}
	
	@Override
	public final boolean contains(Tile tile) {
		return reverseTiles()
				.anyMatch(tile::isAt);
	}
	
	public LinkedListPath finalizePath() {
		return new LinkedListPath(getTiles());
	}
	
	@Override
	public Tile getStart() {
		if(reference != null)
			return reference.getStart();
		return getEnd();
	}
	
	@Override
	public Tile getEnd() {
		return last;
	}
	
	protected ReferencePath(ReferencePath reference, Tile last) {
		this.reference = reference;
		if(reference != null) {
			length = reference.length + 1;
			turns = reference.turns;
		}
		else {
			length = 1;
			turns = 0;
		}
		this.last = last;
	}
	
	protected ReferencePath(Tile start) {
		this(null, start);
	}
	
}
