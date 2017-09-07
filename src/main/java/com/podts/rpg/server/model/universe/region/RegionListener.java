package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.Tile;

/**
 * A interface that can respond to {@link Entity entities} moving into, inside and out of {@link Region regions}.
 *
 */
public interface RegionListener {
	
	/**
	 * Called when an {@link Entity} enters this a given {@link Region region}.
	 * @param r - The {@link Region region} that the {@link Entity} entered in.
	 * @param e - The {@link Entity} that entered the {@link Region region}.
	 * @param moveType - The type of movement the {@link Entity} made to enter given {@link Region region}.
	 */
	public default boolean onEntityEnter(Region r, Entity e, MoveType moveType) {
		return true;
	}
	
	/**
	 * Called when an {@link Entity entity } moves inside a given {@link Region region}.
	 * The {@link Entity entity} was already in the region and is not leaving just moving from one location to another.
	 * @param r - The {@link Region region} that the {@link Entity} is moving in.
	 * @param e - The {@link Entity} that moved inside {@link Region}.
	 * @param moveType - The type of movement the {@link Entity} made inside the {@link Region region}.
	 */
	public default boolean onEntityMove(Region r, Entity e, MoveType moveType) {
		return true;
	}
	
	/**
	 * Called when an {@link Entity} leaves a given {@link Region region}.
	 * @param r - The {@link Region region} that the {@link Entity} left.
	 * @param e - The {@link Entity} that left the {@link Region region}.
	 * @param moveType - The type of movement the {@link Entity} made to leave given {@link Region region}.
	 */
	public default boolean onEntityLeave(Region r, Entity e, MoveType moveType) {
		return true;
	}
	
	/**
	 * Called when a {@link Tile tile} is changed in a given {@link Region region}.
	 * @param r - The {@link Region region} that the {@link Tile tile} was changed in.
	 * @param oldTile - The old {@link Tile tile}.
	 * @param newTile - The new {@link Tile tile}.
	 */
	public default boolean onTileChange(Region r, Tile oldTile, Tile newTile) {
		return true;
	}
	
}
