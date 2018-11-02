package com.podts.rpg.server.model.universe;

import java.util.Objects;

/**
 * A {@link VectorLocation} that stores a reference to a {@link Plane}.
 * @author David
 *
 */
public class VectorPlaneLocation extends VectorLocation {
	
	public static final VectorPlaneLocation construct(Plane plane, Vector vector) {
		return new VectorPlaneLocation(Objects.requireNonNullElse(plane, Space.NOWHERE_PLANE)
				, Objects.requireNonNullElse(vector, Vector.ZERO));
	}
	
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
