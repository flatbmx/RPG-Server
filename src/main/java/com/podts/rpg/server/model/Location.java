package com.podts.rpg.server.model;

import com.podts.rpg.server.model.universe.World;

public final class Location implements Locatable {
	
	private final World world;
	private final int x, y, z;
	
	public World getWorld() {
		return world;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public double distance(Location other) {
		if(other == null) throw new IllegalArgumentException("Cannot calculate distance from null Location.");
		return Math.sqrt(Math.pow(x-other.x, 2) + Math.pow(y-other.y, 2));
	}
	
	public Location move(int deltaX, int deltaY, int deltaZ) {
		return new Location(world, x + deltaX, y + deltaY, z + deltaZ);
	}
	
	@Override
	public Location getLocation() {
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o instanceof Location) {
			Location other = (Location) o;
			return getX() == other.getX() && getY() == other.getY();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int) (x*27 + y*439);
	}
	
	public Location(World world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public enum MoveType {
		CREATE(),
		MOVE(),
		TELEPORT(),
		DESTROY();
	}
	
}
