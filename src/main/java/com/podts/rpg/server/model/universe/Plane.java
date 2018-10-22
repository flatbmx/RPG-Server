package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.region.IncompleteRegion;
import com.podts.rpg.server.model.universe.region.PollableRegion;

/**
 * A 2d Spatial collection of all {@link Location Locations} who's Z coordinate matches this Planes Z height in a specific {@link Space}.
 * This class requires only that {@link HasSpace#getSpace getSpace()}, {@link #getTiles()}, {@link #getEntities()} and {@link #getRegions()} is implemented.
 * For the entire life of any instance of this class will always return the same space.
 * @author David
 *
 */
public abstract class Plane extends IncompleteRegion implements Comparable<Plane>, HasPlane {
	
	public static final Plane validate(Plane plane) {
		if(plane == null)
			return Space.NOWHERE.getPlane();
		return plane;
	}
	
	public static final Comparator<Plane> BOTTOM_TO_TOP_COMPARATOR = (a,b) -> a.getZ() - b.getZ(),
			TOP_TO_BOTTOM_COMPARATOR = BOTTOM_TO_TOP_COMPARATOR.reversed();
	
	private final int z;
	
	public final int getZ() {
		return z;
	}
	
	@Override
	public Plane getPlane() {
		return this;
	}
	
	@Override
	public boolean contains(Location point) {
		return equals(point.getPlane());
	}
	
	public final boolean isAbove(Plane other) {
		return getZ() > other.getZ();
	}
	
	public final boolean isBelow(Plane other) {
		return getZ() < other.getZ();
	}
	
	public boolean isTop() {
		return equals(getSpace().getTopPlane());
	}
	
	public boolean isBottom() {
		return equals(getSpace().getBottomPlane());
	}
	
	public Plane shift(int dz) {
		return getSpace().getPlane(getZ() + dz);
	}
	
	/**
	 * Returns a {@link Collection} that contains all current {@link Tile tiles} that this Plane consists of.
	 * The collection that is returned may change over subsiquent calls.
	 * @return Collection of all tiles in this Plane.
	 */
	public abstract Collection<Tile> getTiles();
	
	abstract Stream<Tile> allTiles();
	
	public Stream<Tile> tiles() {
		return allTiles()
				.filter(Tile::isNotVoid);
	}
	
	public Tile getTile(HasLocation l) {
		if(!contains(l))
			return null;
		return tiles()
				.filter(l::isAt)
				.findAny()
				.orElse(null);
	}
	
	/**
	 * Returns a {@link Collection} of all {@link Entity entities} that are contained inside this Plane.
	 * @return Collection of all entities inside this plane.
	 */
	public abstract Collection<Entity> getEntities();
	
	public Stream<Entity> entities() {
		return getEntities().stream();
	}
	
	public Stream<Player> players() {
		return entities()
				.filter(Player::is)
				.map(PlayerEntity.class::cast)
				.map(PlayerEntity::getPlayer);
	}
	
	/**
	 * Returns a {@link Collection} that contains all of the registered {@link PollableRegion region}.
	 * @return Collection of all currently registered regions that contain points in this plane.
	 */
	public abstract Collection<PollableRegion> getRegions();
	
	public Stream<PollableRegion> regions() {
		return getRegions().stream();
	}
	
	public Location createLocation(final int x, final int y) {
		return getSpace().createLocation(x, y, getZ());
	}
	
	@Override
	public final boolean contains(final Locatable l) {
		if(l == null)
			return false;
		return l.isInPlane(getZ()) &&
				isInSameSpace(l);
	}
	
	@Override
	public final int compareTo(Plane other) {
		return BOTTOM_TO_TOP_COMPARATOR.compare(this, other);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(!(o instanceof Plane)) return false;
		Plane other = (Plane) o;
		return isInSameSpace(other) &&
				getZ() == other.getZ();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSpace(), getZ());
	}
	
	@Override
	public String toString() {
		return "[" + getSpace() + " | Plane " + getZ() + "]";
	}
	
	Plane(final int z) {
		this.z = z;
	}
	
}
