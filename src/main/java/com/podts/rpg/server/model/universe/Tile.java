package com.podts.rpg.server.model.universe;

import java.util.Objects;

public class Tile implements Registerable, Locatable {
	
	public enum TileType {
		VOID(false),
		GRASS(),
		DIRT(),
		SAND(),
		WATER(false);
		
		private final boolean traversable;
		
		public boolean isTraversable() {
			return traversable;
		}
		
		private TileType() {
			traversable = true;
		}
		
		private TileType(boolean travel) {
			traversable = travel;
		}
		
	}
	
	private final TileType type;
	private final Location location;
	
	public final TileType getType() {
		return type;
	}
	
	@Override
	public final Location getLocation() {
		return location;
	}
	
	public final boolean isTraversable() {
		return type.isTraversable();
	}
	
	@Override
	public String toString() {
		return getType().toString() + " - " + getLocation();
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
	
	protected Tile(TileType type, Location location) {
		this.type = type;
		this.location = location;
	}
	
}
