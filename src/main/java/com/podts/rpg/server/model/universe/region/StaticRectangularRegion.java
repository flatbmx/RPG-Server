package com.podts.rpg.server.model.universe.region;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;

public class StaticRectangularRegion extends SimpleRegion implements RectangularRegion, PollableRegion {
	
	private final Location center;
	private final List<Location> corners;
	private final int xWidth, yWidth;
	
	@Override
	public Location getCenter() {
		return center;
	}

	@Override
	public List<Location> getCorners() {
		return corners;
	}

	@Override
	public int getXWidth() {
		return xWidth;
	}

	@Override
	public int getYWidth() {
		return yWidth;
	}
	
	@Override
	public Set<Location> getPoints() {
		final Set<Location> pointSet = new HashSet<Location>();
		final Location topLeft = getCorner(Corner.TOP_LEFT), bottomRight = getCorner(Corner.BOTTOM_RIGHT);
		for(int y=topLeft.getY(); y <= bottomRight.getY(); ++y) {
			for(int x=topLeft.getX(); x <= bottomRight.getX(); ++x) {
				pointSet.add(getCenter().getSpace().createLocation(x, y, getCenter().getZ()));
			}
		}
		return pointSet;
	}
	
	protected StaticRectangularRegion(final Locatable center, final int xWidth, final int yWidth) {
		this.center = center.getLocation();
		this.xWidth = xWidth;
		this.yWidth = yWidth;
		
		final Location[] corners = new Location[4];
		final Corner[] cornerValues = Corner.values();
		for(int i=0; i<4; ++i) {
			corners[i] = getCenter().move(cornerValues[i].dx * xWidth/2, cornerValues[i].dy * yWidth/2, 0);
		}
		this.corners = Collections.unmodifiableList(Arrays.asList(corners));
	}
	
	protected StaticRectangularRegion(final Locatable center, final int width) {
		this(center, width, width);
	}
	
	protected StaticRectangularRegion(final Locatable topLeft, final Locatable bottomRight) {
		Objects.requireNonNull(topLeft);
		Objects.requireNonNull(bottomRight);
		xWidth = bottomRight.getLocation().getX() - topLeft.getLocation().getX();
		yWidth = bottomRight.getLocation().getY() - topLeft.getLocation().getY();
		center = topLeft.getSpace().createLocation(topLeft.getLocation().getX() + xWidth/2
				, topLeft.getLocation().getY() + yWidth/2, topLeft.getLocation().getZ());
		final Location[] corners = new Location[4];
		final Corner[] cornerValues = Corner.values();
		for(int i=0; i<4; ++i) {
			corners[i] = getCenter().move(cornerValues[i].dx * xWidth/2, cornerValues[i].dy * yWidth/2, 0);
		}
		this.corners = Collections.unmodifiableList(Arrays.asList(corners));
	}
	
}
