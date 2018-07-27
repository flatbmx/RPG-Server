package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.region.IncompleteRegion;
import com.podts.rpg.server.model.universe.region.PollableRegion;

public abstract class Plane extends IncompleteRegion implements Comparable<Plane>, HasPlane {
	
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
	
	public abstract Collection<Tile> getTiles();
	
	public Stream<Tile> tiles() {
		return getTiles().stream();
	}
	
	public Tile getTile(Locatable l) {
		if(!contains(l)) return null;
		return tiles()
				.filter(l::isAt)
				.findAny()
				.orElse(null);
	}
	
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
	
	public abstract Collection<PollableRegion> getRegions();
	
	public Stream<PollableRegion> regions() {
		return getRegions().stream();
	}
	
	public final Location createLocation(final int x, final int y) {
		return getSpace().createLocation(x, y, getZ());
	}
	
	@Override
	public final boolean contains(final Locatable l) {
		if(l == null) return false;
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
		return getSpace().equals(other.getSpace()) &&
				getZ() == other.getZ();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSpace(), getZ());
	}
	
	@Override
	public String toString() {
		return "[" + getSpace() + " - Plane " + getZ() + "]";
	}
	
	Plane(final int z) {
		this.z = z;
	}
	
}
