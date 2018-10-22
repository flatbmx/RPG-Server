package com.podts.rpg.server.model.universe;

public abstract class PrimativeLocation extends Location {
	
	protected final int x, y, z;
	
	@Override
	public final int getX() {
		return x;
	}
	
	@Override
	public final int getY() {
		return y;
	}
	
	@Override
	public final int getZ() {
		return z;
	}
	
	public PrimativeLocation(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
}
