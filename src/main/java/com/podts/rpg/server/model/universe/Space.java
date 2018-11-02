package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.Utils;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.TileElement.TileType;
import com.podts.rpg.server.model.universe.path.Path;
import com.podts.rpg.server.model.universe.path.PathFinder;
import com.podts.rpg.server.model.universe.path.ReferencePathFinder;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.TilePacket;

/**
 * A set of {@link Location locations} where all return this when {@link Location#getSpace()} returns this.
 * @author David
 *
 */
public abstract class Space implements HasSpace {
	
	public static final Space validate(Space space) {
		return Objects.requireNonNullElse(space, Space.OBLIVION);
	}
	
	private static final class Oblivion extends Space {
		
		private final Plane plane = new Plane(0) {
			
			@Override
			public Space getSpace() {
				return OBLIVION;
			}
			
			@Override
			public Collection<Tile> getTiles() {
				return Collections.singleton(Space.NOWHERE_TILE);
			}

			@Override
			Stream<Tile> allTiles() {
				return Stream.of(Space.NOWHERE_TILE);
			}

			@Override
			public Collection<Entity> getEntities() {
				return Collections.emptySet();
			}

			@Override
			public Collection<PollableRegion> getRegions() {
				return Collections.emptySet();
			}
			
			@Override
			public String toString() {
				return "[Nowhere Plane]";
			}
			
		};
		
		@Override
		public Location createLocation(int x, int y, int z) {
			return Space.NOWHERE;
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
		public Stream<Tile> surroundingTiles(HasLocation center, int distance) {
			return Stream.empty();
		}
		
		@Override
		Stream<Tile> doSurroundingTiles(HasLocation center, int distance) {
			return Stream.empty();
		}
		
		@Override
		public Collection<? extends Plane> getPlanes() {
			return Collections.singleton(plane);
		}
		
		@Override
		public
		Location moveEntity(Entity entity, MoveType update, int dx, int dy, int dz) {
			return null;
		}
		
		@Override
		public final String toString() {
			return "Oblivion";
		}

		@Override
		public Optional<Tile> getTile(Location point) {
			return Optional.of(Space.NOWHERE_TILE);
		}

		@Override
		public Stream<Entity> nearbyEntities(HasLocation l) {
			return Stream.empty();
		}

		@Override
		public
		Space moveEntity(Entity entity, Location newLocation, MoveType update) {
			return this;
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
			Optional<Tile> tile = getTile(center.shift(dx, dy));
			if(dx == distance) {
				++dy;
				dx = -1 * distance;
			} else
				++dx;
			return tile.get();
		}
		
		SurroundingIterable(HasLocation center, int distance) {
			this.center = center.getLocation();
			distance = Math.abs(distance);
			this.distance = distance;
			dx = -1 * distance;
			dy = -1 * distance;
		}
		
	}
	
	/**
	 * The empty space in which {@link #NOWHERE} inhabits.
	 */
	public static final Oblivion OBLIVION = new Oblivion();
	
	/**
	 * The empty plane in which {@link #NOWHWERE} inhabits.
	 */
	public static final Plane NOWHERE_PLANE = OBLIVION.plane;
	
	/**
	 * The non-null representation of nowhere.
	 * When a method is to return a {@link Location} but does not have one it should return this rather than null.
	 */
	public static final Location NOWHERE = new PrimativeSpaceLocation(OBLIVION, 0, 0, 0) {
		@Override public final Collection<Location> getLocations() {return Collections.emptySet();}
		@Override public final Stream<Location> locations() {return Stream.empty();}
		@Override public final boolean isAt(Locatable loc) {return loc.isNowhere();}
		@Override public final boolean isAt(HasLocation loc) {return this == loc.getLocation();}
		@Override public final Tile getTile() {return NOWHERE_TILE;}
		@Override public final Plane getPlane() {return Space.OBLIVION.plane;}
		@Override public final PrimativeSpaceLocation shift(int dx, int dy, int dz) {return this;}
		@Override public final PrimativeSpaceLocation shift(int dx, int dy) {return this;}
		@Override public final Stream<Location> traceEvery(Direction dir, int increment) {return Stream.of(this);}
		@Override public final Stream<Location> bitraceEvery(Direction dir, int increment) {return Stream.of(this);}
		@Override public final PrimativeSpaceLocation clone() {return this;}
		@Override public final String toString() {return "[Nowhere]";}
	};
	
	/**
	 * The non-null tile representation of nowhere.
	 */
	public static final Tile NOWHERE_TILE = new Tile(TileType.VOID, NOWHERE) {
		@Override public final Collection<Location> getLocations() {return Collections.emptySet();}
		@Override public final Stream<Location> locations() {return Stream.empty();}
		@Override public final boolean isAt(Locatable loc) {return loc.isNowhere();}
		@Override public final boolean isAt(HasLocation loc) {return loc.isNowhere();}
		@Override public final boolean isNowhere() {return true;}
		@Override public final Plane getPlane() {return Space.OBLIVION.plane;}
		@Override public final Tile shift(int dx, int dy, int dz) {return this;}
		@Override public final Tile shift(int dx, int dy) {return this;}
		@Override public final Stream<Tile> traceEvery(Direction dir, int increment) {return Stream.of(this);}
		@Override public final String toString() {return "[Nowhere Tile]";}
	};
	
	private final Location origin = createLocation(0, 0, 0);
	private final PathFinder pathFinder;
	
	protected static final int getZ(Locatable l) {
		return l.getPlane().getZ();
	}
	
	protected PathFinder getPathFinder() {
		return pathFinder;
	}
	
	public abstract Location createLocation(int x, int y, int z);
	
	public Location createLocation(Vector vector) {
		return createLocation(vector.getX(), vector.getY(), vector.getZ());
	}
	
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
	
	public Optional<? extends Plane> getPlane(int z) {
		return planes()
				.filter(plane -> plane.getZ() == z)
				.findAny();
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
		Optional<? extends Plane> plane = getPlane(z);
		if(!plane.isPresent())
			return Stream.empty();
		return plane.get().allTiles();
	}
	
	public Stream<Tile> tiles(int z) {
		return allTiles(z)
				.filter(Tile::isNotVoid);
	}
	
	public Stream<Tile> allTiles(int... z) {
		return Arrays.stream(z)
				.mapToObj(this::getPlane)
				.filter(Optional::isPresent)
				.map(Optional::get)
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
	
	public Stream<? extends Tile> nearbyTiles(HasLocation l, double distance) {
		return InfiniteSurroundingTileSpliterator.surroundingTiles(l.getTile(), (int)distance)
				.filter(t -> t.isInRange(l, distance));
	}
	
	public Stream<? extends Tile> nearbyWalkingTiles(HasLocation l, int distance) {
		return InfiniteSurroundingTileSpliterator.surroundingTiles(l.getTile(), distance)
				.filter(tile -> tile.isInWalkingRange(l, distance));
	}
	
	public Optional<Tile> getTile(Location point) {
		return point.getPlane().getTile(point);
	}
	
	public final boolean tileExists(Location point) {
		return getTile(point).isPresent();
	}
	
	public Stream<Tile> tiles(PollableRegion r) {
		return r.points()
				.map(this::getTile)
				.filter(Optional::isPresent)
				.map(Optional::get);
	}
	
	public Space setTile(Tile tile, TileElement element) {
		Objects.requireNonNull(tile, "Cannot set TileElement of null Tile!");
		Objects.requireNonNull(element, "Cannot set Tile with null TileElement!");
		if(!contains(tile))
			throw new IllegalArgumentException("Cannot set Tile from different space!");
		if(element.isLinked())
			throw new IllegalArgumentException("Cannot set a Tile with a TileElement that is already associated with a Tile!");
		return doSetTile(tile, element);
	}
	
	public Space setTile(Tile tile, TileType type) {
		Objects.requireNonNull(tile, "Cannot set TileElement of null Tile!");
		Objects.requireNonNull(type, "Cannot set Tile with null TileType!");
		if(!contains(tile))
			throw new IllegalArgumentException("Cannot set Tile from different space!");
		return doSetTile(tile, new TileElement(type));
	}
	
	protected Space doSetTile(Tile tile, TileElement element) {
		tile.getElement().onLeave();
		handleTileChange(tile, element);
		tile.element.tile = null;
		tile.element = element;
		element.tile = tile;
		element.onEnter();
		sendToNearbyPlayers(tile, TilePacket.constructCreate(tile));
		return this;
	}
	
	private void handleTileChange(Tile tile, TileElement element) {
		Iterator<TileListener> it = tile.tileListenerIterator();
		while(it.hasNext()) {
			TileListener h = it.next();
			if(h.onChange(tile, element)) {
				it.remove();
				h.onRemove(tile);
			}
		}
	}
	
	Space updateTile(Tile tile) {
		Iterator<TileListener> it = tile.tileListenerIterator();
		while(it.hasNext()) {
			TileListener h = it.next();
			if(h.onUpdate(tile)) {
				it.remove();
				h.onRemove(tile);
			}
		}
		return this;
	}
	
	/**
	 * Sets the tile located at the given point.
	 * It is recommended to use {@link Space#setTile(Tile, TileElement)} instead of this method when able
	 * due to possible tile lookup overhead.
	 * @param element The new TileElement for the given point.
	 * @param point The location of the tile to change.
	 * @return
	 */
	public final Optional<Tile> setTile(TileElement element, Location point) {
		Objects.requireNonNull(element, "Cannot set Tile with null TileElement!");
		Objects.requireNonNull(point, "Canot set Tile at null Location!");
		if(!contains(point))
			throw new IllegalArgumentException("Cannot set Tile from another Space!");
		if(element.isLinked())
			throw new IllegalArgumentException("Cannot set Tile with a TileElement that is already associated with another Tile!");
		return doSetTile(element, point);
	}
	
	protected Optional<Tile> doSetTile(TileElement element, Location point) {
		Optional<Tile> tileOpt = getTile(point);
		if(tileOpt.isPresent()) {
			doSetTile(tileOpt.get(), element);
		}
		return tileOpt;
	}
	
	public boolean isTraversable(Tile tile) {
		if(tile == null)
			return false;
		return doIsTraversable(tile);
	}
	
	boolean doIsTraversable(Tile tile) {
		return tile.getType().isTraversable();
	}
	
	protected final void sendToNearbyPlayers(HasLocation l, Player except, Packet... packets) {
		nearbyPlayers(l)
		.filter(p -> !p.equals(except))
		.forEach(player -> {
			player.sendPacket(packets);
		});
	}
	
	protected final void sendToNearbyPlayers(HasLocation l, Packet... packets) {
		nearbyPlayers(l)
		.forEach(player -> {
			player.sendPacket(packets);
		});
	}
	
	public final Collection<Player> getNearbyPlayers(final HasLocation l, double distance) {
		return entities()
				.filter(e -> e.isInRange(l, distance))
				.filter(e -> Player.is(e))
				.map(e -> PlayerEntity.class.cast(e))
				.map(oe -> oe.getPlayer())
				.collect(Collectors.toList());
	}
	
	public final Collection<Player> getNearbyPlayers(HasLocation l) {
		return nearbyPlayers(l)
				.collect(Collectors.toSet());
	}
	
	public final Stream<Player> nearbyPlayers(HasLocation l) {
		Utils.assertNull(l, "Cannot find nearby players from null locatable.");
		Utils.assertArg(!contains(l), "Cannot find nearby players from location in another world.");
		return doNearbyPlayers(l.getLocation());
	}
	
	/**
	 * It is recommended to implement this method in the sub class that extends World.
	 * Default Implementation filters nearby entities for PlayerEntitys.
	 * @param point
	 * @return
	 */
	protected Stream<Player> doNearbyPlayers(Location point) {
		return nearbyEntities(point)
				.filter(Player::is)
				.map(PlayerEntity.class::cast)
				.map(PlayerEntity::getPlayer);
	}
	
	public abstract Stream<Entity> nearbyEntities(HasLocation l);
	
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
		if(isInDifferentSpace(center))
			return Stream.empty();
		return doSurroundingTiles(center, Math.abs(distance));
	}
	
	Stream<Tile> doSurroundingTiles(HasLocation center, int distance) {
		return InfiniteSurroundingTileSpliterator.surroundingTiles(center.getTile(), distance);
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
		Optional<? extends Plane> plane = getPlane(z);
		if(!plane.isPresent())
			return Stream.empty();
		return plane.get().regions();
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
				.filter(Optional::isPresent)
				.map(Optional::get)
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
	
	public abstract Space moveEntity(Entity entity, Location newLocation, MoveType update);
	
	public abstract Location moveEntity(Entity entity, MoveType update, int dx, int dy, int dz);

	public final Location moveEntity(Entity entity, Direction dir) {
		return moveEntity(entity, MoveType.UPDATE, dir.getX(), dir.getY(), 0);
	}
	
	public Optional<Path> findPath(HasLocation start, HasLocation finish, int maxLength) {
		return getPathFinder().findPath(start, finish, maxLength);
	}
	
	public Optional<Path> findPath(HasLocation start, HasLocation finish) {
		return getPathFinder().findPath(start, finish);
	}
	
	Space(PathFinder pathFinder) {
		this.pathFinder = pathFinder;
	}
	
	Space() {
		this(new ReferencePathFinder());
	}
	
}
