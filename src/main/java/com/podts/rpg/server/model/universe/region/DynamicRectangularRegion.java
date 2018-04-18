package com.podts.rpg.server.model.universe.region;

import java.util.Arrays;
import java.util.List;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.World;

class DynamicRectangularRegion extends SimpleRegion implements RectangularRegion {
	
	private final Locatable corner1, corner2;
	
	final World getWorld() {
		return corner1.getWorld();
	}
	
	private final Location[] getTwoCorners() {
		final Location[] result = new Location[2];
		final Location l1 = corner1.getLocation(), l2 = corner2.getLocation();
		if(l1.getX() <= l2.getX()) {
			if(l1.getY() <= l2.getY()) {
				//l1 is top-left
				result[0] = l1;
				result[1] = l2;
			} else {
				//l1 is bottom-left
				result[0] = getWorld().createLocation(l1.getX(), l2.getY(), l1.getZ());
				result[1] = l2;
			}
		} else {
			if(l2.getY() <= l1.getY()) {
				//l2 is top-left
				result[0] = l2;
				result[1] = l1;
			} else {
				//l2 is bottom-left
				result[0] = getWorld().createLocation(l2.getX(), l1.getY(), l2.getZ());
				result[1] = l1;
			}
		}
		return result;
	}
	
	@Override
	public Location getCenter() {
		return Regions.findCenter(corner1, corner2);
	}

	@Override
	public List<Location> getCorners() {
		final Location[] twoCorners = getTwoCorners();
		final List<Location> result = Arrays.asList(new Location[4]);
		
		result.set(0, twoCorners[0]);
		result.set(1, twoCorners[0].move(findXWidth(twoCorners), 0, 0));
		result.set(2, twoCorners[1]);
		result.set(3, twoCorners[0].move(0, findYWidth(twoCorners), 0));
		
		return result;
	}

	@Override
	public int getXWidth() {
		final Location[] twoCorners = getTwoCorners();
		return findXWidth(twoCorners);
	}
	
	private int findXWidth(final Location[] twoCorners) {
		return Math.abs(twoCorners[0].getX() - twoCorners[1].getX());
	}
	
	@Override
	public int getYWidth() {
		final Location[] twoCorners = getTwoCorners();
		return findYWidth(twoCorners);
	}
	
	private int findYWidth(final Location[] twoCorners) {
		return Math.abs(twoCorners[0].getY() - twoCorners[1].getY());
	}
	
	@Override
	public final boolean isSquare() {
		final Location[] twoCorners = getTwoCorners();
		return findXWidth(twoCorners) == findYWidth(twoCorners);
	}
	
	DynamicRectangularRegion(final Locatable corner1, final Locatable corner2) {
		this.corner1 = corner1;
		this.corner2 = corner2;
	}
	
}