package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.region.PollableRegion;

public abstract class Space {
	
	public abstract Location createLocation(int x, int y, int z);
	
	public abstract boolean isRegistered(Registerable r);
	public abstract boolean register(Registerable r);
	public abstract boolean deRegister(Registerable r);
	
	public abstract Stream<? extends Plane> planes();
	
	public Plane getPlane(int z) {
		return planes()
				.filter(plane -> plane.getZ() == z)
				.findAny()
				.orElse(null);
	}
	
	public Stream<Tile> tiles() {
		return planes()
				.flatMap(Plane::tiles);
	}
	
	public boolean hasTile(Location point) {
		return tiles()
				.anyMatch(t -> t.isAt(point));
	}
	
	public Stream<Tile> tiles(int... z) {
		return Arrays.stream(z)
				.mapToObj(this::getPlane)
				.filter(plane -> plane != null)
				.flatMap(Plane::tiles);
	}
	
	public Stream<Tile> tiles(int z) {
		Plane plane = getPlane(z);
		if(plane == null) return Stream.empty();
		return plane.tiles();
	}
	
	public Stream<Tile> nearbyTiles(Locatable l, double distance) {
		return tiles(l.getLocation().getZ())
				.filter(tile -> tile.isInRange(l, distance));
	}
	
	public Stream<Tile> nearbyWalkingTiles(Locatable l, int distance) {
		return tiles(l.getLocation().getZ())
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
				.filter(t -> t.isAt(l))
				.findAny()
				.orElse(null);
	}
	
	public abstract Stream<PollableRegion> regions();
	
	public Stream<PollableRegion> regions(Locatable l) {
		return regions()
				.filter(r -> r.contains(l));
	}
	
	public abstract Stream<Entity> entities();
	
	public Stream<Entity> entities(Locatable l) {
		return entities()
				.filter(e -> e.isAt(l));
	}
	
	public Stream<Entity> entities(int z) {
		return entities()
				.filter(e -> e.isInPlane(z));
	}
	
	public Stream<Entity> nearbyEntities(Locatable l, double distance) {
		return entities()
				.filter(e -> l.isInRange(e, distance));
	}
	
	public Stream<Entity> nearbyWalkingEntities(Locatable l, int distance) {
		return entities()
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
				.map(PlayerEntity.class::cast)
				.map(PlayerEntity::getPlayer);
	}
	
	abstract Location moveEntity(Entity entity, MoveType update, int dx, int dy, int dz);

	final Location moveEntity(Entity entity, Direction dir) {
		return moveEntity(entity, MoveType.UPDATE, dir.getX(), dir.getY(), 0);
	}
	
	Space() {
		
	}
	
}
