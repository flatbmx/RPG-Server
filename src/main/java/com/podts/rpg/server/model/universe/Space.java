package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.region.PollableRegion;

public abstract class Space implements HasSpace {
	
	protected static final int getZ(Locatable l) {
		return l.getLocation().getZ();
	}
	
	public abstract Location createLocation(int x, int y, int z);
	
	public abstract boolean isRegistered(Registerable r);
	public abstract boolean register(Registerable r);
	public abstract boolean deRegister(Registerable r);
	
	@Override
	public final Space getSpace() {
		return this;
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
	
	/**
	 * Returns a Stream of all non-void tiles in this space.
	 * @return Stream consisting of all non-void tiles in this space.
	 */
	public Stream<Tile> tiles() {
		return planes()
				.flatMap(Plane::tiles);
	}
	
	public boolean hasTile(Location point) {
		return tiles(point.getZ())
				.anyMatch(point::isAt);
	}
	
	public Stream<Tile> tiles(int z) {
		Plane plane = getPlane(z);
		if(plane == null) return Stream.empty();
		return plane.tiles();
	}
	
	public Stream<Tile> tiles(int... z) {
		return Arrays.stream(z)
				.mapToObj(this::getPlane)
				.filter(Objects::nonNull)
				.flatMap(Plane::tiles);
	}
	
	public Stream<Tile> nearbyTiles(Locatable l, double distance) {
		return tiles(getZ(l))
				.filter(tile -> tile.isInRange(l, distance));
	}
	
	public Stream<Tile> nearbyWalkingTiles(Locatable l, int distance) {
		return tiles(getZ(l))
				.filter(tile -> tile.isInWalkingRange(l, distance));
	}
	
	public Stream<Tile> tiles(PollableRegion r) {
		return r.points()
				.map(this::getTile);
	}
	
	public abstract Tile createTile(TileType type, Location point);
	
	public final Tile setTile(TileType type, int x, int y, int z) {
		return setTile(type, createLocation(x, y, z));
	}
	
	public final Tile setTile(TileType type, Location point) {
		Tile tile = createTile(type, point);
		register(tile);
		return tile;
	}
	
	public Tile getTile(Locatable l) {
		return tiles()
				.filter(l::isAt)
				.findAny()
				.orElse(null);
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
	
	public Stream<PollableRegion> regions(Locatable l) {
		return regions(getZ(l))
				.filter(r -> r.contains(l));
	}
	
	public Stream<Entity> entities() {
		return planes()
				.flatMap(Plane::entities);
	}
	
	public Stream<Entity> entities(Locatable l) {
		return entities(getZ(l))
				.filter(l::isAt);
	}
	
	public Stream<Entity> entities(int... z) {
		return Arrays.stream(z)
				.mapToObj(this::getPlane)
				.filter(Objects::nonNull)
				.flatMap(Plane::entities);
	}
	
	public Stream<Entity> nearbyEntities(Locatable l, double distance) {
		return entities(getZ(l))
				.filter(e -> l.isInRange(e, distance));
	}
	
	public Stream<Entity> nearbyWalkingEntities(Locatable l, int distance) {
		return entities(getZ(l))
				.filter(e -> l.isInWalkingRange(e, distance));
	}
	
	public Stream<Player> players() {
		return entities()
				.filter(Player::is)
				.map(e -> ((PlayerEntity)e).getPlayer());
	}
	
	public Stream<Player> nearbyPlayers(Locatable l, double distance) {
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
