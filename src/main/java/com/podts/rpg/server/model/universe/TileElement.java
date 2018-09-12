package com.podts.rpg.server.model.universe;

public class TileElement {
	
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

	Tile tile;
	private final TileType type;
	
	protected Tile getTile() {
		return tile;
	}
	
	public final TileType getType() {
		return type;
	}
	
	/**
	 * Triggers a tile update for this elements parent Tile.
	 * @return This TileElement for chaining.
	 */
	protected final TileElement update() {
		tile.update();
		return this;
	}
	
	public final boolean isLinked() {
		return getTile() != null;
	}
	
	/**
	 * Called when this TileElement is attached to a Tile.
	 */
	protected void onEnter() {
		
	}
	
	/**
	 * Called when this TileElement is removed from a Tile.
	 */
	protected void onLeave() {
		
	}
	
	@Override
	public String toString() {
		return getType().toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(o instanceof TileElement) {
			TileElement other = (TileElement) o;
			return getType().equals(other.getType());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getType().hashCode();
	}
	
	protected TileElement(TileType type) {
		this.type = type;
	}
	
}
