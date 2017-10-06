package com.podts.rpg.server.model.universe.region;

import java.util.Collection;

import com.podts.rpg.server.model.universe.Locatable;

/**
 * A subset of the Universe.
 *  It also *may* support listeners that will be fired when an entity enters or leaves the region.
 *  Some implementations may not support {@link RegionListener RegionListeners}.
 *  The only method that is guaranteed to be supported is {@link #contains(Locatable) contains}.
 *
 */
public interface Region {
	
	public @interface Dynamic {
		public boolean changesShape();
		public boolean changesVolume();
		public boolean changesLocation();
	}
	
	/**
	 * Returns an un-Modifiable view of all the current {@link RegionListener}s that is listening to this Region.
	 * @return Collection of all the listeners.
	 */
	public Collection<RegionListener> getRegionListeners();
	
	/**
	 * Adds a given {@link RegionListener} to this Region.
	 * If The RegionListener is already added this will do nothing.
	 * @param newRegionListener - The RegionListener to add to this Region.
	 * @return The Region for chaining.
	 */
	public Region addRegionListener(RegionListener newRegionListener);
	
	/**
	 * Removes a given {@link RegionListener} from this Region.
	 * If The RegionListener is not added this will do nothing.
	 * @param newRegionListener - The RegionListener to remove this Region.
	 * @return The Region for chaining.
	 */
	public Region removeRegionListener(RegionListener regionListener);
	
	/**
	 * Determines if a given point is in this Region.
	 * @param point - The given point in question.
	 * @return True if the point is in this Region, false otherwise.
	 */
	public boolean contains(Locatable point);
	
}
