package com.podts.rpg.server.model.universe;

import java.util.Objects;

public class PrimativePlaneLocation extends Location {
	
	public static final PrimativePlaneLocation construct(Plane plane, int x, int y) {
		Objects.requireNonNull(plane, "Cannot create PrimativePlaneLocation with a null plane!");
		return new PrimativePlaneLocation(plane, x, y);
	}
	
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
			return shift(dx, dy);
		} else
			return new PrimativePlaneLocation(getSpace().getPlane(getZ() + dz).get(), getX() + dx, getY() + dy);
	}
	
	@Override
	public PrimativePlaneLocation shift(int dx, int dy) {
		return new PrimativePlaneLocation(getPlane(), getX() + dx, getY() + dy);
	}
	
	@Override
	public PrimativePlaneLocation clone() {
		return this;
	}
	
	PrimativePlaneLocation(Plane plane, int x, int y) {
		this.plane = plane;
		this.x = x;
		this.y = y;
	}
	
}
