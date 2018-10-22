package com.podts.rpg.server.model.universe;

/**
 * A {@link VectorLocation} that stores a reference to a {@link Plane}.
 * @author David
 *
 */
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
