package com.podts.rpg.server.model.universe;

public interface HasPlane extends HasSpace {
	
	public Plane getPlane();
	
	@Override
	public default Space getSpace() {
		return getPlane().getSpace();
	}
	
}
