package com.podts.rpg.server.model.universe;

import java.util.Objects;

public class CompleteLocation extends SimpleLocation {
	
	protected final Space space;
	
	@Override
	public final Space getSpace() {
		return space;
	}
	
	@Override
	public Plane getPlane() {
		return getSpace().getPlane(getZ());
	}
	
	@Override
	public CompleteLocation shift(int dx, int dy, int dz) {
		return new CompleteLocation(space, x + dx, y + dy, z + dz);
	}
	
	@Override
	public CompleteLocation clone() {
		return new CompleteLocation(space, x, y, z);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSpace(), getX(), getY(), getZ());
	}
	
	public CompleteLocation(Space space, int x, int y, int z) {
		super(x, y, z);
		this.space = Objects.requireNonNull(space, "Cannot create CompleteLocation with a null Space!");
	}
	
}
