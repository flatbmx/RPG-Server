package com.podts.rpg.server.model.universe;

public abstract class Location implements Locatable {
	
	public abstract World getWorld();
	public abstract int getX();
	public abstract int getY();
	public abstract int getZ();
	
	public abstract Location move(int dx, int dy, int dz);
	
	public final double distance(Locatable other) {
		if(other == null) throw new IllegalArgumentException("Cannot calculate distance between a null Location.");
		if(!getWorld().equals(other.getWorld())) throw new IllegalArgumentException("Cannot calculate distance between points in different worlds.");
		final Location otherPoint = other.getLocation();
		if(getZ() != otherPoint.getZ()) throw new IllegalArgumentException("Cannot calculate distance between points in different Z planes.");
		return Math.sqrt(Math.pow(getX() - otherPoint.getX(), 2) + Math.pow(getY() - otherPoint.getY(), 2));
	}
	
	@Override
	public final Location getLocation() {
		return this;
	}
	
	protected Location() {
		
	}
	
	public enum MoveType {
		CREATE(),
		UPDATE(),
		DESTROY();
	}
	
}
