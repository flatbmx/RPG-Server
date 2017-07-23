package com.podts.rpg.server.model;

public class Location implements Locatable, Cloneable {
	
	private System system;
	private double x, y;
	
	public System getSystem() {
		return system;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double distance(Location other) {
		if(other == null) throw new IllegalArgumentException("Cannot calculate distance from null Location.");
		if(!getSystem().equals(other.getSystem())) throw new IllegalArgumentException("Cannot calculate distance from location in different system");
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
		return new Location(other.getSystem(), other.getX(), other.getY());
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o instanceof Location) {
			Location other = (Location) o;
			return getSystem().equals(other.getSystem()) && getX() == other.getX() && getY() == other.getY();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int) (x*27 + y*439);
	}
	
	public Location(System system) {
		this.system = system;
	}
	
	public Location(System system, double x, double y) {
		this.system = system;
		this.x = x;
		this.y = y;
	}
	
}
