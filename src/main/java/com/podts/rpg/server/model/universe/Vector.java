package com.podts.rpg.server.model.universe;

public class Vector {
	
	private final int x, y, z;
	
	public final int getX() {
		return x;
	}
	
	public final int getY() {
		return y;
	}
	
	public final int getZ() {
		return z;
	}
	
	Vector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
}
