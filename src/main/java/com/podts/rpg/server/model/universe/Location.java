package com.podts.rpg.server.model.universe;

public abstract class Location implements Locatable {
	
	public enum Direction {
		UP(0,-1),
		DOWN(0,1),
		LEFT(-1,0),
		RIGHT(1,0);
		
		private static final Direction[] vals = Direction.values();
		private final int dx, dy;
		
		public int getX() {
			return dx;
		}
		
		public int getY() {
			return dy;
		}
		
		public static final Direction getFromLocations(Location first, Location second) {
			int dx = second.getX() - first.getX();
			int dy = second.getY() - first.getY();
			if(dx != 0) dx = dx/Math.abs(dx);
			if(dy != 0) dy = dy/Math.abs(dy);
			if(dx != 0 && dy != 0) return null;
			for(Direction dir : vals) {
				if(dir.dx == dx && dir.dy == dy) return dir;
			}
			return null;
		}
		
		public Location MoveFromLocation(Location origin) {
			return origin.move(dx, dy, 0);
		}
		
		private Direction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
		
	}
	
	public enum MoveType {
		CREATE(),
		UPDATE(),
		DESTROY();
	}
	
	@Override
	public abstract World getWorld();
	
	public abstract int getX();
	public abstract int getY();
	public abstract int getZ();
	
	public abstract Location move(int dx, int dy, int dz);
	
	@Override
	public final double distance(final Locatable other) {
		if(other == null) throw new IllegalArgumentException("Cannot calculate distance between a null Location.");
		if(!getWorld().equals(other.getWorld())) throw new IllegalArgumentException("Cannot calculate distance between points in different worlds.");
		final Location otherPoint = other.getLocation();
		if(getZ() != otherPoint.getZ()) throw new IllegalArgumentException("Cannot calculate distance between points in different Z planes.");
		return distance(otherPoint);
	}
	
	final double distance(final Location otherPoint) {
		return Math.sqrt(Math.pow(getX() - otherPoint.getX(), 2) + Math.pow(getY() - otherPoint.getY(), 2));
	}
	
	@Override
	public final Location getLocation() {
		return this;
	}
	
	protected Location() {
		
	}
	
}
