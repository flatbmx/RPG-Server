package com.podts.rpg.server.model.universe;

public class PrimativePlaneLocation extends Location {
	
	private final Plane plane;
	private final int x, y;
	
	@Override
	public final Plane getPlane() {
		return plane;
	}
	
	@Override
	public final int getX() {
		return x;
	}
	
	@Override
	public final int getY() {
		return y;
	}
	
	@Override
	public PrimativePlaneLocation shift(int dx, int dy, int dz) {
		if(dz == 0) {
			return new PrimativePlaneLocation(getPlane(), getX() + dx, getY() + dy);
		} else
			return new PrimativePlaneLocation(getSpace().getPlane(getZ() + dz), getX() + dx, getY() + dy);
	}
	
	@Override
	public PrimativePlaneLocation shift(int dx, int dy) {
		return new PrimativePlaneLocation(getPlane(), getX() + dx, getY() + dy);
	}
	
	@Override
	public PrimativePlaneLocation clone() {
		return new PrimativePlaneLocation(getPlane(), getX(), getY());
	}
	
	public PrimativePlaneLocation(Plane plane, int x, int y) {
		this.plane = plane;
		this.x = x;
		this.y = y;
	}
	
}
