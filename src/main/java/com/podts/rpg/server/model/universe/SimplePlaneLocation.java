package com.podts.rpg.server.model.universe;

public abstract class SimplePlaneLocation extends Location {
	
	private final int x, y;
	
	@Override
	public int getX() {
		return x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	public SimplePlaneLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
}
