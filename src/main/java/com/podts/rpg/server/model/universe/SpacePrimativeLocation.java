package com.podts.rpg.server.model.universe;

import java.util.Objects;

public class SpacePrimativeLocation extends PrimativeLocation {
	
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
	public SpacePrimativeLocation shift(int dx, int dy, int dz) {
		return new SpacePrimativeLocation(getSpace(), getX() + dx, getY() + dy, getZ() + dz);
	}
	
	@Override
	public SpacePrimativeLocation clone() {
		return new SpacePrimativeLocation(getSpace(), getX(), getY(), getZ());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSpace(), getX(), getY(), getZ());
	}
	
	public SpacePrimativeLocation(Space space, int x, int y, int z) {
		super(x, y, z);
		this.space = Objects.requireNonNull(space, "Cannot create CompleteLocation with a null Space!");
	}
	
}
