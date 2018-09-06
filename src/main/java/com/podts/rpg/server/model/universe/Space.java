package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.region.PollableRegion;

public abstract class Space implements HasSpace {
	
	private static final class Oblivion extends Space {
		
		@Override
		public Location createLocation(int x, int y, int z) {
			return new CompleteLocation(this, x, y, z);
		}

		@Override
		public boolean isRegistered(Registerable r) {
			return false;
		}

		@Override
		public boolean register(Registerable r) {
			return false;
		}

		@Override
		public boolean deRegister(Registerable r) {
			return false;
		}

		@Override
		public Collection<? extends Plane> getPlanes() {
			return Collections.emptyList();
		}
		
		@Override
		Location moveEntity(Entity entity, MoveType update, int dx, int dy, int dz) {
			return null;
		}
		
		@Override
		public final String toString() {
			return "Oblivion";
		}

		@Override
		public Tile getTile(Location point) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	private final class SurroundingIterable implements Iterable<Tile>, Iterator<Tile> {
		
		private final Location center;
		private final int distance;
		private int dx, dy;
		
		@Override
		public Iterator<Tile> iterator() {
			return this;
		}
		
		@Override
		public boolean hasNext() {
			return dy <= distance;
		}

		@Override
		public Tile next() {
			if(!hasNext())
				throw new NoSuchElementException();
			
			if(dx == 0 && dy == 0)
				dx = 1;
			Tile tile = getTile(center.shift(dx, dy));
			if(dx == distance) {
				++dy;
				dx = -1 * distance;
			} else
				++dx;
			return tile;
		}
		
		SurroundingIterable(HasLocation center, int distance) {
			this.center = center.getLocation();
			distance = Math.abs(distance);
			this.distance = distance;
			dx = -1 * distance;
			dy = -1 * distance;
		}
		
	}
	
	private static final Oblivion oblivion = new Oblivion();
	private static final Location nowhere = new CompleteLocation(oblivion, 0, 0, 0) {
		@Override
		public final String toString() {
			return "Nowhere";
		}
	};
	
	public static final Location getNowhere() {
		return nowhere;
	}
	
	private final Location origin = createLocation(0, 0, 0);
	
	protected static final int getZ(Locatable l) {
		return l.getPlane().getZ();
	}
	
	public abstract Location createLocation(int x, int y, int z);
	
	public abstract boolean isRegistered(Registerable r);
	public abstract boolean register(Registerable r);
	public abstract boolean deRegister(Registerable r);
	
	@Override
	public final Space getSpace() {
		return this;
	}
	
	public Location getOrigin() {
		return origin;
	}
	
	public abstract Collection<? extends Plane> getPlanes();
	
	public Stream<? extends Plane> planes() {
		return getPlanes().stream();
	}
	
	public final boolean hasPlane(int z) {
		return getPlane(z) != null;
	}
	
	public Plane getPlane(int z) {
		return planes()
				.filter(plane -> plane.getZ() == z)
				.findAny()
				.orElse(null);
	}
	
	public Plane getTopPlane() {
		return planes()
				.sorted(Plane.TOP_TO_BOTTOM_COMPARATOR)
				.findFirst()
				.orElse(null);
	}
	
	public Plane getBottomPlane() {
		return planes()
				.sorted(Plane.BOTTOM_TO_TOP_COMPARATOR)
				.findFirst()
				.orElse(null);
	}
	
	Stream<Tile> allTiles() {
		return planes()
				.flatMap(Plane::allTiles);
	}
	
	/**
	 * Returns a Stream of all non-void tiles in this space.
	 * @return Stream consisting of all non-void tiles in this space.
	 */
	public Stream<Tile> tiles() {
		return planes()
				.flatMap(Plane::tiles);
	}
	
	public boolean hasTile(Locatable loc) {
		return tiles(getZ(loc))
				.anyMatch(loc::isAt);
	}
	
	Stream<Tile> allTiles(int z) {
		Plane plane = getPlane(z);
		if(plane == null) return Stream.empty();
		return plane.allTiles();
	}
	
	public Stream<Tile> tiles(int z) {
		return allTiles(z)
				.filter(Tile::isNotVoid);
	}
	
	public Stream<Tile> allTiles(int... z) {
		return Arrays.stream(z)
				.mapToObj(this::getPlane)
				.filter(Objects::nonNull)
				.flatMap(Plane::allTiles);
	}
	
	public Stream<Tile> tiles(int... z) {
		return allTiles()
				.filter(Tile::isNotVoid);
	}
	
	Stream<Tile> allTiles(Locatable l) {
		if(l == null) return Stream.empty();
		return allTiles(l.getPlane());
	}
	
	Stream<Tile> allTiles(Plane plane) {
		return plane.allTiles();
	}
	
	public Stream<Tile> nearbyTiles(HasLocation l, double distance) {
		return tiles(getZ(l))
				.filter(tile -> tile.isInRange(l, distance));
	}
	
	public Stream<Tile> nearbyWalkingTiles(HasLocation l, int distance) {
		return tiles(getZ(l))
				.filter(tile -> tile.isInWalkingRange(l, distance));
	}
	
	public Tile getTile(Location point) {
		return point.getPlane().getTile(point);
	}
	
	public Stream<Tile> tiles(PollableRegion r) {
		return r.points()
				.map(this::getTile);
	}
	
	public final Tile setTile(Tile tile, Location point) {
		register(tile);
		return tile;
	}
	
	public final Tile setTile(Tile tile, int x, int y, int z) {
		return setTile(tile, createLocation(x, y, z));
	}
	
	public boolean isTraversable(Tile tile) {
		if(tile == null)
			return false;
		return tile.getType().isTraversable();
	}
	
	public Collection<Tile> getSurroundingTiles(HasLocation center) {
		return surroundingTiles(center)
				.collect(Collectors.toSet());
	}
	
	public Iterable<Tile> getSurroundingTilesIterable(HasLocation center, int distance) {
		return new SurroundingIterable(center, distance);
	}
	
	public Iterable<Tile> getSurroundingTilesIterable(HasLocation center) {
		return getSurroundingTilesIterable(center, 1);
	}
	
	public Stream<Tile> surroundingTiles(HasLocation center, int distance) {
		if(isInDifferentSpace(center)) return Stream.empty();
		return Streams.stream(getSurroundingTilesIterable(center, distance));
	}
	
	public Stream<Tile> surroundingTiles(HasLocation loc) {
		return surroundingTiles(loc, 1);
	}
	
	public boolean contains(Locatable l) {
		return equals(l.getSpace());
	}
	
	public Stream<PollableRegion> regions() {
		return planes()
				.flatMap(Plane::regions)
				.distinct();
	}
	
	public Stream<PollableRegion> regions(int z) {
		Plane plane = getPlane(z);
		if(plane == null) return Stream.empty();
		return plane.regions();
	}
	
	public Stream<PollableRegion> regions(HasLocation l) {
		if(isInDifferentSpace(l)) return Stream.empty();
		return regions(getZ(l))
				.filter(r -> r.contains(l));
	}
	
	public Stream<Entity> entities() {
		return planes()
				.flatMap(Plane::entities);
	}
	
	public Stream<Entity> entities(HasLocation l) {
		if(isInDifferentSpace(l))
			return Stream.empty();
		return entities(getZ(l))
				.filter(l::isAt);
	}
	
	public Stream<Entity> entities(int... z) {
		return Arrays.stream(z)
				.mapToObj(this::getPlane)
				.filter(Objects::nonNull)
				.flatMap(Plane::entities);
	}
	
	public Stream<Entity> nearbyEntities(HasLocation l, double distance) {
		if(isInDifferentSpace(l)) return Stream.empty();
		return entities(getZ(l))
				.filter(e -> l.isInRange(e, distance));
	}
	
	public Stream<Entity> nearbyWalkingEntities(Locatable l, int distance) {
		if(isInDifferentSpace(l)) return Stream.empty();
		return entities(getZ(l))
				.filter(e -> l.isInWalkingRange(e, distance));
	}
	
	public Stream<Player> players() {
		return entities()
				.filter(Player::is)
				.map(e -> ((PlayerEntity)e).getPlayer());
	}
	
	public Stream<Player> nearbyPlayers(HasLocation l, double distance) {
		if(isInDifferentSpace(l)) return Stream.empty();
		return nearbyEntities(l, distance)
				.filter(Player::is)
				.map(e -> ((PlayerEntity)e).getPlayer());
	}
	
	abstract Location moveEntity(Entity entity, MoveType update, int dx, int dy, int dz);

	final Location moveEntity(Entity entity, Direction dir) {
		return moveEntity(entity, MoveType.UPDATE, dir.getX(), dir.getY(), 0);
	}
	
	Space() {
		
	}
	
}
