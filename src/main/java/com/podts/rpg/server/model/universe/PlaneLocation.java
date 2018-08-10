package com.podts.rpg.server.model.universe;

public class PlaneLocation extends Location {
	
	private final Plane plane;
	private final int x, y;
	
	@Override
	public Plane getPlane() {
		return plane;
	}
	
	@Override
	public int getX() {
		return x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	@Override
	public PlaneLocation shift(int dx, int dy, int dz) {
		if(dz == 0) {
			return new PlaneLocation(getPlane(), getX() + dx, getY() + dy);
		} else
			return new PlaneLocation(getSpace().getPlane(getZ() + dz), getX() + dx, getY() + dy);
	}
	
	@Override
	public PlaneLocation shift(int dx, int dy) {
		return new PlaneLocation(getPlane(), getX() + dx, getY() + dy);
	}
	
	@Override
	public PlaneLocation clone() {
		return new PlaneLocation(getPlane(), getX(), getY());
	}
	
	public PlaneLocation(Plane plane, int x, int y) {
		this.plane = plane;
		this.x = x;
		this.y = y;
	}
	
}
