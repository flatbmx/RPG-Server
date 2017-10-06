package com.podts.rpg.server.model.universe.region;

import java.util.List;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

public interface RectangularRegion extends CenteredRegion, PolygonRegion {
	
	public static enum Corner {
		TOP_LEFT(-1,-1),
		TOP_RIGHT(1,-1),
		BOTTOM_RIGHT(1,1),
		BOTTOM_LEFT(-1,1);
		
		protected final int dx, dy;
		
		private Corner(final int dx, final int dy) {
			this.dx = dx;
			this.dy = dy;
		}
		
	}
	
	@Override
	public default boolean contains(Locatable loc) {
		Location l = loc.getLocation();
		if(Math.abs(getCenter().getX() - l.getX()) > getXWidth()) return false;
		if(Math.abs(getCenter().getY() - l.getY()) > getYWidth()) return false;
		return true;
	}
	
	public List<Location> getCorners();
	
	public default Location getCorner(Corner c) {
		return getCorners().get(c.ordinal());
	}
	
	public int getXWidth();
	public int getYWidth();
	
	public default boolean isSquare() {
		return getXWidth() == getYWidth();
	}
	
}