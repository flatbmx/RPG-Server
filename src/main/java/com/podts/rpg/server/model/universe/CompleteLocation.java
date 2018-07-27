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
	public CompleteLocation shift(final int dx, final int dy, final int dz) {
		return new CompleteLocation(space, x + dx, y + dy, z + dz);
	}
	
	@Override
	public CompleteLocation shift(final int dx, final int dy) {
		return shift(dx, dy, 0);
	}
	
	@Override
	public CompleteLocation clone() {
		return new CompleteLocation(space, x, y, z);
	}
	
	@Override
	public final boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(o instanceof Location) {
			Location other = (Location) o;
			return isInSameSpace(other) &&
					getX() == other.getX() &&
					getY() == other.getY() &&
					getZ() == other.getZ();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSpace(), getX(), getY(), getZ());
	}
	
	public CompleteLocation(final Space space, final int x, final int y, final int z) {
		super(x, y, z);
		this.space = space;
	}
	
}
