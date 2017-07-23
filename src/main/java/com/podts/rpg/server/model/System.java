package com.podts.rpg.server.model;

public class System {
	
	private final String name;
	private final double radius;
	
	public String getName() {
		return name;
	}
	
	public double getRadius() {
		return radius;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public System(String name, double radius) {
		this.name = name;
		this.radius = radius;
	}
	
}
