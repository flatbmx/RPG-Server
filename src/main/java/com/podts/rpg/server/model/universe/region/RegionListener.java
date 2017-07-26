package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location.MoveType;

public interface RegionListener {
	
	/**
	 * Called when an {@link Entity} enters this a given {@link Region}.
	 * @param r - The {@link Region} that the {@link Entity} entered in.
	 * @param e - The {@link Entity} that entered the {@link Region}.
	 * @param moveType - The type of movement the {@link Entity} made to enter given {@link Region}.
	 */
	public void onEnter(Region r, Entity e, MoveType moveType);
	
	/**
	 * Called when an {@link Entity} leaves a given {@link Region}.
	 * @param r - The {@link Region} that the {@link Entity} left.
	 * @param e - The {@link Entity} that left the {@link Region}.
	 * @param moveType - The type of movement the {@link Entity} made to leave given {@link Region}.
	 */
	public void onLeave(Region r, Entity e, MoveType moveType);
	
}
