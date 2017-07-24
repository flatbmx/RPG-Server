package com.podts.rpg.server.model;

public class Location implements Locatable, Cloneable {
	
	private double x, y;
	private int z;
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public double distance(Location other) {
		if(other == null) throw new IllegalArgumentException("Cannot calculate distance from null Location.");
		return Math.sqrt(Math.pow(x-other.x, 2) + Math.pow(y-other.y, 2));
	}
	
	public void set(double newX, double newY) {
		x = newX;
		y = newY;
	}
	
	public void move(double deltaX, double deltaY) {
		x += deltaX;
		y += deltaY;
	}
	
	@Override
	public Location getLocation() {
		return this;
	}
	
	public Location clone(Location other) {
		return new Location(other.getX(), other.getY(), other.getZ());
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
	
	public Location() {
		
	}
	
	public Location(double x, double y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
}
