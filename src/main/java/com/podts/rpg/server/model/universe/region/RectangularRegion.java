package com.podts.rpg.server.model.universe.region;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

public interface RectangularRegion extends PolygonRegion {
	
	public static enum Corner {
		TOP_LEFT(0,0),
		TOP_RIGHT(1,0),
		BOTTOM_RIGHT(1,1),
		BOTTOM_LEFT(0,1);
		
		private static final List<Corner> values;
		private final int dx, dy;
		
		static {
			values = Collections.unmodifiableList(Arrays.asList(Corner.values()));
		}
		
		public static final List<Corner> getValues() {
			return values;
		}
		
		public static final Stream<Corner> stream() {
			return values.stream();
		}
		
		public final int getX() {
			return dx;
		}
		
		public final int getY() {
			return dy;
		}
		
		private Corner(final int dx, final int dy) {
			this.dx = dx;
			this.dy = dy;
		}
		
	}
	
	@Override
	public default boolean contains(Locatable loc) {
		Location l = loc.getLocation();
		Location topLeft = getCorner(Corner.TOP_LEFT);
		if(Math.abs(topLeft.getX() - l.getX()) > getXWidth()) return false;
		if(Math.abs(topLeft.getY() - l.getY()) > getYWidth()) return false;
		return true;
	}
	
	public default Location getCorner(Corner c) {
		return getCorners().get(c.ordinal());
	}
	
	public int getXWidth();
	public int getYWidth();
	
	public default boolean isSquare() {
		return getXWidth() == getYWidth();
	}
	
}