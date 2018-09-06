package com.podts.rpg.server.model.universe;

import java.util.Objects;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.Direction;

public class Tile extends Spatial implements Registerable {
	
	public enum TileType {
		VOID(false),
		GRASS(),
		DIRT(),
		SAND(),
		WATER(false);
		
		private final boolean traversable;
		
		boolean isTraversable() {
			return traversable;
		}
		
		private TileType(boolean travel) {
			traversable = travel;
		}
		
		private TileType() {
			this(true);
		}
		
	}
	
	private final TileType type;
	
	public final Tile getTile() {
		return this;
	}
	
	public final TileType getType() {
		return type;
	}
	
	public final boolean isVoid() {
		return TileType.VOID.equals(getType());
	}
	
	public final boolean isNotVoid() {
		return !isVoid();
	}
	
	public final boolean isTraversable() {
		return getSpace().isTraversable(this);
	}
	
	public Stream<Tile> traceTo(Location point) {
		if(isInDifferentSpace(point))
			return Stream.empty();
		
		Direction dir = Direction.get(getLocation(), point);
		if(dir == null)
			return Stream.empty();
		
		return trace(dir)
				.limit(walkingDistance(point) + 1);
	}
	
	public Stream<Tile> traceEvery(Direction dir, int increment) {
		return Stream.iterate(this, tile -> tile.shift(dir, increment));
	}
	
	public Stream<Tile> trace(Direction dir) {
		return traceEvery(dir, 1);
	}
	
	public Tile shift(int dx, int dy, int dz) {
		return getLocation().shift(dx, dy, dz).getTile();
	}
	
	public Tile shift(int dx, int dy) {
		return shift(dx, dy, 0);
	}
	
	public Tile shift(Direction dir, int distance) {
		return shift(dir.getX(distance), dir.getY(distance));
	}
	
	public Tile shift(Direction dir) {
		return shift(dir, 1);
	}
	
	@Override
	public String toString() {
		return "[" + getType().toString() + " " + getLocation() + "]";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getType(), getLocation());
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(o instanceof Tile) {
			Tile t = (Tile) o;
			return getType().equals(t.getType()) &&
					getLocation().equals(t.getLocation());
		}
		return false;
	}
	
	public Tile(TileType type, Location location) {
		super(location);
		this.type = type;
	}
	
	public Tile(TileType type) {
		this(type, null);
	}
	
}
