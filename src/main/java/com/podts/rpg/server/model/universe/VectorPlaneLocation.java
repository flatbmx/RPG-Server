package com.podts.rpg.server.model.universe;

public class VectorPlaneLocation extends VectorLocation {
	
	private final Plane plane;
	
	@Override
	public final Plane getPlane() {
		return plane;
	}
	
	VectorPlaneLocation(Plane plane, Vector vector) {
		super(vector);
		this.plane = plane;
	}

}
