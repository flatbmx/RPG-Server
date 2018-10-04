package com.podts.rpg.server.model.universe;

import java.util.Collection;

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
	
	@SuppressWarnings("unchecked")
	public default <P extends HasPlane> boolean isInPlane(P... planes) {
		Plane plane = getPlane();
		for(P p : planes) {
			if(!plane.equals(p.getPlane()))
				return false;
		}
		return true;
	}
	
	public default <P extends HasPlane> boolean isInPlane(Collection<P> planes) {
		Plane plane = getPlane();
		for(P p : planes) {
			if(!plane.equals(p.getPlane()))
				return false;
		}
		return true;
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
