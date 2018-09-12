package com.podts.rpg.server.model.universe;

public interface HasPlane extends HasSpace {
	
	public Plane getPlane();
	
	@Override
	public default Space getSpace() {
		return getPlane().getSpace();
	}
	
	public default boolean isInPlane(int z) {
		return getPlane().getZ() == z;
	}
	
	public default boolean isInPlane(Plane plane) {
		return getPlane().equals(plane);
	}
	
	public default boolean isInPlane(Locatable l) {
		return isInPlane(l.getPlane());
	}
	
	public default boolean isBetweenPlanes(int minZ, int maxZ) {
		if(minZ > maxZ) {
			int tempZ = minZ;
			minZ = maxZ;
			maxZ = tempZ;
		}
		int z = getPlane().getZ();
		return z >= minZ &&
				z <= maxZ;
	}
	
	public default boolean isBetweenPlanes(Plane a, Plane b) {
		if(a.isInDifferentSpace(b)) return false;
		return isBetweenPlanes(a.getZ(), b.getZ());
	}
	
}
