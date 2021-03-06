package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.Utils;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.StaticChunkWorld.Chunk.ChunkTile;
import com.podts.rpg.server.model.universe.TileElement.TileType;
import com.podts.rpg.server.model.universe.region.IncompleteRegion;
import com.podts.rpg.server.model.universe.region.PollableMonitoringRegion;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.RectangularRegion;
import com.podts.rpg.server.model.universe.region.Regions;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.TilePacket;

public final class StaticChunkWorld extends World {
	
	private static final int DEFAULT_CHUNK_SIZE = 25;
	private static final int DEFAULT_CHUNK_DEPTH = 1;
	
	private final Map<Integer,ChunkPlane> planes = new HashMap<>(),
			safePlanes = Collections.unmodifiableMap(planes);
	
	private final Map<Integer,Player> players = new HashMap<>();
	private final Collection<Player> safePlayers = Collections.unmodifiableCollection(players.values());
	
	private final Map<Integer,Entity> entities = new HashMap<>();
	
	private final Collection<PollableRegion> registeredRegions = new HashSet<>();
	private final Map<PollableRegion,Collection<Chunk>> cachedRegionChunks = new HashMap<>();
	
	private final int chunkSize;
	private final int chunkDepth;
	
	private ChunkPlane bottomPlane, topPlane;
	
	@Override
	public ChunkPlane getTopPlane() {
		return topPlane;
	}
	
	@Override
	public ChunkPlane getBottomPlane() {
		return bottomPlane;
	}
	
	private final static class ChunkCoordinate {
		
		private final int x,y,z;
		private final int hash;
		
		int getX() {
			return x;
		}
		
		int getXDifference(ChunkCoordinate other) {
			return getX() - other.getX();
		}
		
		int getY() {
			return y;
		}
		
		int getYDifference(ChunkCoordinate other) {
			return getY() - other.getY();
		}
		
		int getZ() {
			return z;
		}
		
		public int distance(ChunkCoordinate other) {
			return getXDifference(other) + getYDifference(other);
		}
		
		public ChunkCoordinate shift(int dx, int dy, int dz) {
			return new ChunkCoordinate(getX() + dx, getY() + dy, getZ() + dz);
		}
		
		public ChunkCoordinate shift(int dx, int dy) {
			return shift(dx, dy, 0);
		}
		
		public ChunkCoordinate shift(Direction dir) {
			return shift(dir.getX(), dir.getY());
		}
		
		@Override
		public String toString() {
			return "[ChkCrd : " + getX() + ", " + getY() + ", " + getZ() + "]";
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o instanceof ChunkCoordinate) {
				ChunkCoordinate other = (ChunkCoordinate) o;
				return getX() == other.getX()
						&& getY() == other.getY()
						&& getZ() == other.getZ();
			}
			return false;
		}
		
		protected ChunkCoordinate(final int x, final int y, final int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			hash = Objects.hash(x, y, z);
		}
		
	}
	
	
	
	final class Chunk extends IncompleteRegion implements RectangularRegion, PollableMonitoringRegion, HasPlane {
		
		final class ChunkTile extends Tile {
			
			@Override
			public StaticChunkWorld getSpace() {
				return StaticChunkWorld.this;
			}
			
			final Chunk getChunk() {
				return Chunk.this;
			}
			
			public ChunkTile(CLocation location) {
				super(location);
			}
			
			public ChunkTile(TileType type, CLocation location) {
				super(type, location);
			}
			
		}
		
		private final ChunkPlane plane;
		private final ChunkCoordinate coord;
		private final CLocation topLeft;
		
		boolean generated = false;
		
		private final ChunkTile[][] tiles = new ChunkTile[getChunkSize()][getChunkSize()];
		
		private final Map<Direction,Chunk> neighbors = new EnumMap<Direction,Chunk>(Direction.class);
		
		private final Map<Integer,Player> players = new HashMap<>(),
				safePlayers = Collections.unmodifiableMap(players);
		
		private final Map<Integer,Entity> entities = new HashMap<>(),
				safeEntities = Collections.unmodifiableMap(entities);
		
		private final Set<PollableRegion> regions = new HashSet<>(),
				safeRegions = Collections.unmodifiableSet(regions);
		
		@Override
		public StaticChunkWorld getSpace() {
			return StaticChunkWorld.this;
		}
		
		@Override
		public ChunkPlane getPlane() {
			return plane;
		}
		
		@Override
		public String toString() {
			return "Chunk " + getCoordinate();
		}
		
		Chunk getNeighbor(Direction dir) {
			return neighbors.get(dir);
		}
		
		int chunkSize() {
			return getSpace().chunkSize;
		}
		
		@Override
		public int getXWidth() {
			return chunkSize();
		}
		
		@Override
		public int getYWidth() {
			return chunkSize();
		}
		
		@Override
		public boolean isSquare() {
			return true;
		}
		
		ChunkCoordinate getCoordinate() {
			return coord;
		}
		
		int getZ() {
			return getCoordinate().z;
		}
		
		Stream<PollableRegion> regions() {
			return safeRegions.stream();
		}
		
		boolean addRegion(PollableRegion r) {
			return regions.add(r);
		}
		
		boolean removeRegion(PollableRegion r) {
			return regions.remove(r);
		}
		
		private void initiateTiles() {
			for(int i=0; i<tiles.length; ++i) {
				for(int j=0; j<tiles[i].length; ++j) {
					tiles[i][j] = new ChunkTile(topLeft.shift(i, j));
				}
			}
		}
		
		@Override
		public Collection<Location> getPoints() {
			return points()
					.collect(Collectors.toSet());
		}
		
		@Override
		public Stream<Location> points() {
			if(!isGenerated()) {
				return generatePoints();
			}
			return allTiles()
					.map(Tile::getLocation);
		}
		
		Stream<Location> generatePoints() {
			return IntStream.range(0, chunkSize())
					.mapToObj(Integer::valueOf)
					.flatMap(j -> {
						return IntStream.range(0, chunkSize())
								.mapToObj(i -> new PrimativeSpaceLocation(getSpace(), topLeft.getX() + i, topLeft.getY() + j, getZ()));
					});
		}
		
		final Stream<ChunkTile> allTiles() {
			if(!isGenerated())
				return Stream.empty();
			return Stream.of(tiles)
					.flatMap(Stream::of);
		}
		
		@Override
		public final Stream<ChunkTile> tiles() {
			return allTiles()
					.filter(Tile::isNotVoid);
		}
		
		@Override
		public Collection<Entity> getEntities() {
			return safeEntities.values();
		}
		
		@Override
		public Stream<Player> players() {
			return safePlayers.values().stream();
		}
		
		private ChunkTile getTile(CLocation point) {
			return getTile( ((getChunkSize()) - topLeft.getXDiff(point)) % getChunkSize()
					, ((getChunkSize()) - topLeft.getYDiff(point)) % getChunkSize());
		}
		
		ChunkTile getTile(int x, int y) {
			if(!isSet())
				set();
			return tiles[x][y];
		}
		
		private boolean isSet() {
			return tiles[0][0].getElement() != null;
		}
		
		private void set() {
			for(int j=0; j<getChunkSize(); ++j) {
				for(int i=0; i<getChunkSize(); ++i) {
					
				}
			}
		}
		
		void setTile(ChunkTile newTile, int x, int y) {
			tiles[x][y] = newTile;
		}
		
		@Override
		public Chunk addEntity(Entity entity) {
			if(entity instanceof PlayerEntity) {
				PlayerEntity pE = (PlayerEntity) entity;
				addPlayer(pE.getPlayer());
			}
			entities.put(entity.getID(), entity);
			if(!(entity.getLocation() instanceof CLocation)) {
				entity.setLocation(new CLocation(entity.getLocation()));
			}
			((CLocation)entity.getLocation()).chunk = this;
			return this;
		}
		
		@Override
		public Chunk removeEntity(Entity entity) {
			if(entity instanceof PlayerEntity) {
				Player player = ((PlayerEntity)entity).getPlayer();
				removePlayer(player);
			}
			entities.remove(entity.getID());
			return this;
		}
		
		void addPlayer(Player player) {
			players.put(player.getID(), player);
		}
		
		void removePlayer(Player player) {
			players.remove(player.getID());
		}
		
		final boolean isGenerated() {
			return generated;
		}
		
		/**
		 * Generates this Chunk if it is not already generated or the overwrite boolean is true.
		 * @param overwrite - If the chunk should be re-generated.
		 * @return This Chunk for chaining.
		 */
		Chunk generate(final boolean overwrite) {
			if(!isGenerated() || overwrite)
				getSpace().generateChunk(this);
			return this;
		}
		
		/**
		 * Generates this chunks tiles using this worlds generator.
		 * <br>
		 * If this chunk is already generated this will <b>not</b> regenerate it.
		 * @return This Chunk for chaining.
		 */
		Chunk generate() {
			return generate(false);
		}
		
		@Override
		public List<Location> getCorners() {
			return corners()
					.collect(Collectors.toList());
		}
		
		@Override
		public Stream<Location> corners() {
			return Corner.stream()
					.map(this::getCorner);
		}
		
		@Override
		public Location getCorner(Corner c) {
			if(Corner.TOP_LEFT.equals(c)) return topLeft;
			return topLeft.shift(chunkSize() * c.getX(), chunkSize() * c.getY());
		}
		
		@Override
		public boolean contains(Location point) {
			return RectangularRegion.super.contains(point);
		}
		
		protected Chunk(final ChunkPlane plane, final ChunkCoordinate coord) {
			this.plane = plane;
			this.coord = coord;
			int x = coord.getX() * getChunkSize() - (getChunkSize()-1)/2;
			int y = coord.getY() * getChunkSize() - (getChunkSize()-1)/2;
			topLeft = new CLocation(this, x, y, coord.getZ());
			initiateTiles();
		}
		
	}
	
	private final class ChunkPlane extends Plane {
		
		private final Map<ChunkCoordinate,Chunk> chunks = new HashMap<>();
		private final Set<PollableRegion> regions = new HashSet<>(),
				safeRegions = Collections.unmodifiableSet(regions);
		
		@Override
		public StaticChunkWorld getSpace() {
			return StaticChunkWorld.this;
		}
		
		@Override
		public ChunkPlane getPlane() {
			return this;
		}
		
		@Override
		public boolean isTop() {
			return getZ() == getTopPlane().getZ();
		}
		
		@Override
		public boolean isBottom() {
			return getZ() == getBottomPlane().getZ();
		}
		
		@Override
		public Optional<ChunkPlane> shift(int dz) {
			return getSpace().getPlane(getZ() + dz);
		}
		
		Chunk getOrCreateChunk(final ChunkCoordinate coord) {
			synchronized(chunks) {
				return chunks.computeIfAbsent(coord, this::createChunk);
			}
		}
		
		private Chunk createChunk(ChunkCoordinate coord) {
			Chunk chunk = new Chunk(this, coord);
			for(Direction dir : Direction.getAll()) {
				Chunk other = findChunk(coord.shift(dir));
				if(other == null) continue;
				chunk.neighbors.put(dir, other);
				other.neighbors.put(dir.opposite(), chunk);
			}
			return chunk;
		}
		
		public Chunk findChunk(ChunkCoordinate coord) {
			return chunks.get(coord);
		}
		
		Stream<Chunk> chunks() {
			return chunks.values().stream();
		}
		
		Stream<Chunk> generatedChunks() {
			return chunks()
					.filter(Chunk::isGenerated);
		}
		
		@Override
		public Collection<Tile> getTiles() {
			return tiles()
					.collect(Collectors.toSet());
		}
		
		@Override
		Stream<Tile> allTiles() {
			return generatedChunks()
					.flatMap(Chunk::allTiles);
		}
		
		@Override
		public Stream<Tile> tiles() {
			return generatedChunks()
					.flatMap(Chunk::tiles);
		}
		
		@Override
		public Collection<Entity> getEntities() {
			return entities()
					.collect(Collectors.toSet());
		}
		
		@Override
		public Stream<Entity> entities() {
			return generatedChunks()
					.flatMap(Chunk::entities);
		}
		
		@Override
		public Collection<PollableRegion> getRegions() {
			return safeRegions;
		}
		
		private ChunkPlane(final int z) {
			super(z);
		}
		
	}
	
	final class CLocation extends PrimativeLocation {
		
		private Chunk chunk;
		private final int hash;
		
		@Override
		public final StaticChunkWorld getSpace() {
			return StaticChunkWorld.this;
		}
		
		@Override
		public final ChunkPlane getPlane() {
			return getChunk().getPlane();
		}
		
		public ChunkTile getTile() {
			return getGeneratedChunk().getTile(this);
		}
		
		@Override
		public Stream<Entity> entities() {
			return getChunk().entities();
		}
		
		@Override
		public final CLocation shift(final int dx, final int dy, final int dz) {
			final Location sl = getChunk().topLeft;
			final int nX = x + dx, nY = y + dy, nZ = z + dz;
			Chunk c = chunk;
			if(nZ != z ||
					nX < sl.getX() ||
					nY < sl.getY() ||
					nX - sl.getX() >= getChunkSize() ||
					nY - sl.getY() >= getChunkSize()) {
				ChunkCoordinate oldCoord = getChunk().getCoordinate();
				ChunkCoordinate newCoord = getSpace().getCoordinateFromLocation(nX, nY, nZ);
				if(oldCoord.distance(newCoord) == 1)
					c = getChunk().getNeighbor(Direction.get(newCoord.getXDifference(oldCoord), newCoord.getYDifference(oldCoord)).get());
				else
					c = null;
			}
			
			return new CLocation(c, nX, nY, nZ);
		}
		
		@Override
		public final CLocation shift(final int dx, final int dy) {
			return shift(dx, dy, 0);
		}
		
		@Override
		public final int hashCode() {
			return hash;
		}
		
		@Override
		public final boolean equals(Object o) {
			if(this == o) return true;
			if(o == null) return false;
			if(!(o instanceof CLocation)) return false;
			CLocation other = (CLocation) o;
			return getX() == other.getX() &&
					getY() == other.getY() &&
					getZ() == other.getZ() &&
					isInSameSpace(other);
		}
		
		@Override
		public final CLocation clone() {
			return this;
		}
		
		/**
		 * Returns true if this location has a reference to its Chunk already.
		 * @return true if this location has a reference to its Chunk, false otherwise.
		 */
		final boolean hasChunk() {
			return chunk != null;
		}
		
		final Chunk getChunk() {
			if(!hasChunk()) {
				chunk = StaticChunkWorld.this.findChunk(this);
			}
			return chunk;
		}
		
		final Chunk getGeneratedChunk() {
			return getChunk().generate();
		}
		
		private final int computeHash() {
			int n = 17;
			n = n * 31 + getX();
			n = n * 31 + getY();
			n = n * 31 + getZ();
			return n;
		}
		
		private CLocation(final Chunk chunk, final int x, final int y, final int z) {
			super(x, y, z);
			this.chunk = chunk;
			hash = computeHash();
		}
		
		private CLocation(final int x, final int y, final int z) {
			this(null, x, y, z);
		}

		public CLocation(Location location) {
			this(location.getX(), location.getY(), location.getZ());
		}
		
	}
	
	private int getChunkSize() {
		return chunkSize;
	}
	
	private int getChunkDepth() {
		return chunkDepth;
	}
	
	private Chunk chunk(HasLocation l) {
		Location loc = l.getLocation();
		if(loc instanceof CLocation)
			return chunk((CLocation) l.getLocation());
		return chunk(new CLocation(loc.getX(), loc.getY(), loc.getZ()));
	}
	
	private Chunk chunk(CLocation point) {
		return point.getChunk();
	}
	
	public Stream<Chunk> chunks() {
		return planes()
				.flatMap(ChunkPlane::chunks);
	}
	
	public Stream<Chunk> chunks(final int z) {
		Optional<ChunkPlane> plane = getPlane(z);
		if(!plane.isPresent())
			return Stream.empty();
		return plane.get().chunks();
	}
	
	public Stream<Chunk> generatedChunks() {
		return planes()
				.flatMap(ChunkPlane::generatedChunks);
	}
	
	public Stream<Chunk> generatedChunks(final int z) {
		Optional<ChunkPlane> plane = getPlane(z);
		if(!plane.isPresent())
			return Stream.empty();
		return plane.get().generatedChunks();
	}
	
	@Override
	public Collection<ChunkPlane> getPlanes() {
		return safePlanes.values();
	}
	
	@Override
	public Stream<ChunkPlane> planes() {
		return safePlanes.values().stream();
	}
	
	@Override
	public Optional<ChunkPlane> getPlane(final int z) {
		return Optional.ofNullable(safePlanes.get(z));
	}
	
	private ChunkPlane getOrCreatePlane(final int z) {
		return planes.computeIfAbsent(z, this::generatePlane);
	}
	
	private ChunkPlane generatePlane(final int z) {
		final ChunkPlane plane = new ChunkPlane(z);
		if(getTopPlane() == null) {
			topPlane = plane;
			bottomPlane = plane;
		} else if(plane.isAbove(getTopPlane())) {
			topPlane = plane;
		} else if(plane.isBelow(getBottomPlane())) {
			bottomPlane = plane;
		}
		return plane;
	}
	
	private void removePlane(ChunkPlane plane) {
		planes.remove(plane.getZ());
		if(plane.isTop()) {
			topPlane = findTopPlane();
		}
		if(plane.isBottom()) {
			bottomPlane = findBottomPlane();
		}
	}
	
	private ChunkPlane findTopPlane() {
		return (ChunkPlane) super.getTopPlane();
	}
	
	private ChunkPlane findBottomPlane() {
		return (ChunkPlane) super.getBottomPlane();
	}
	
	@Override
	public Stream<Tile> tiles() {
		return generatedChunks()
				.flatMap(Chunk::tiles);
	}
	
	public Stream<Tile> nearbyTiles(HasLocation l) {
		return surroundingChunks(l)
				.flatMap(Chunk::tiles);
	}
	
	@Override
	public Stream<ChunkTile> nearbyTiles(HasLocation l, double distance) {
		int depth = (int)Math.ceil(distance / getChunkSize());
		return surroundingChunks(l, depth)
				.peek(Chunk::generate)
				.flatMap(Chunk::tiles)
				.filter(tile -> tile.isInRange(l, distance));
	}
	
	@Override
	public Stream<ChunkTile> nearbyWalkingTiles(HasLocation l, int distance) {
		int depth = (int)Math.ceil(distance / getChunkSize()) + 1;
		return surroundingChunks(l, depth)
				.peek(Chunk::generate)
				.flatMap(Chunk::tiles)
				.filter(tile -> tile.isInWalkingRange(l, distance));
	}
	
	/**
	 * Returns a stream of chunks that the passed region is in.
	 * @param r - The region thats chunks should be returned.
	 * @return Stream of chunks that the passed region is in.
	 */
	private Stream<Chunk> chunks(PollableRegion r) {
		if(Regions.isStaticRegion(r)) {
			Collection<Chunk> chunks = cachedRegionChunks.get(r);
			if(chunks != null)
				return chunks.stream();
		}
		return r.points()
				.map(this::chunk)
				.unordered()
				.distinct();
	}
	
	/**
	 * Returns a stream of non generated or generated chunks that is surrounding the given point.
	 * @param point - The central point
	 * @param depth - the depth of chunks
	 * @return Stream containing the non generated surrounding chunks of the point
	 */
	private Stream<Chunk> surroundingChunks(final CLocation point, final int depth) {
		ChunkCoordinate center = getCoordinate(point);
		return IntStream.rangeClosed(-depth, depth)
				.mapToObj(Integer::valueOf)
				.flatMap(j -> {
					return IntStream.rangeClosed(-depth, depth)
							.mapToObj(i -> findChunk(center.shift(i, j)));
				});
	}
	
	private Stream<Chunk> surroundingChunks(CLocation point) {
		return surroundingChunks(point, getChunkDepth());
	}
	
	private Stream<Chunk> surroundingChunks(HasLocation l, int depth) {
		return surroundingChunks(cLoc(l.getLocation()), depth);
	}
	
	private Stream<Chunk> surroundingChunks(HasLocation l) {
		return surroundingChunks(cLoc(l.getLocation()));
	}
	
	private Chunk findChunk(final CLocation point) {
		return findChunk(getCoordinate(point));
	}
	
	private Chunk findChunk(final ChunkCoordinate coord) {
		return getOrCreatePlane(coord.z)
				.getOrCreateChunk(coord);
	}
	
	private Chunk findChunkSafely(final ChunkCoordinate coord) {
		return getOrCreatePlane(coord.z)
				.findChunk(coord);
	}
	
	private Chunk getGeneratedChunk(final ChunkCoordinate coord) {
		return findChunk(coord).generate();
	}
	
	private Chunk getGeneratedChunk(final HasLocation point) {
		return getGeneratedChunk(getCoordinate(point.getLocation()));
	}
	
	private ChunkCoordinate getCoordinate(final CLocation point) {
		if(point.hasChunk())
			return point.getChunk().getCoordinate();
		return getCoordinateFromLocation(point.getX(), point.getY(), point.getZ());
	}
	
	private ChunkCoordinate getCoordinate(final Location point) {
		return getCoordinateFromLocation(point.getX(), point.getY(), point.getZ());
	}
	
	private ChunkCoordinate getCoordinateFromLocation(final int x, final int y, final int z) {
		int cx = (int) Math.floor((x+(getChunkSize()-1)/2d)/getChunkSize());
		int cy = (int) Math.floor((y+(getChunkSize()-1)/2d)/getChunkSize());
		return new ChunkCoordinate(cx,cy,z);
	}
	
	/**
	 * Generates the given chunk using this worlds world generator.
	 * <p>
	 * <b>NOTE:</b> This will re-generate already generated chunks.
	 * </p>
	 * @param chunk - The chunk that will be (re)generated.
	 */
	private void generateChunk(final Chunk chunk) {
		getWorldGenerator().doGenerateRectTiles(chunk.tiles, chunk.topLeft);
		chunk.generated = true;
	}
	
	private Chunk shiftChunk(Chunk chunk, int dx, int dy, int dz) {
		return findChunk(chunk.getCoordinate().shift(dx, dy, dz));
	}
	
	private Chunk shiftChunk(Chunk chunk, int dx, int dy) {
		return shiftChunk(chunk, dx, dy, 0);
	}
	
	private Chunk shiftChunk(CLocation point, int dx, int dy, int dz) {
		return shiftChunk(chunk(point), dx, dy, dz);
	}
	
	private Chunk shiftChunk(CLocation point, int dx, int dy) {
		return shiftChunk(point, dx, dy, 0);
	}
	
	@Override
	public Collection<Player> getPlayers() {
		return safePlayers;
	}
	
	@Override
	public Optional<Tile> doGetTile(final Location point) {
		Chunk chunk = getGeneratedChunk(point);
		final Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		return Optional.of(chunk.getTile(x, y));
	}
	
	@Override
	protected void doGetTiles(Tile[][] tiles, Location topLeft) {
		CLocation tL = (CLocation) topLeft;
		Chunk topLeftChunk = tL.getChunk();
		Chunk topRightChunk = tL.shift(tiles.length, 0).getChunk();
		int chunkWidth = topRightChunk.getCoordinate().getX() - topLeftChunk.getCoordinate().getX();
		Chunk bottomLeftChunk = tL.shift(0, tiles[0].length, 0).getChunk();
		int chunkHeight = bottomLeftChunk.coord.getY() - topLeftChunk.coord.getY();
		final int width = tiles.length, height = tiles[0].length;
		//WOW
	}
	
	protected void doSetTile(ChunkTile newTile) {
		Location point = newTile.getLocation();
		Chunk chunk = getGeneratedChunk(point);
		Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		chunk.setTile(newTile, x, y);
	}
	
	public Stream<Entity> nearbyEntities(final Location point, final double distance) {
		final double dist = Math.abs(distance);
		int depth = (int) Math.floor(getChunkSize()/distance) + 1;
		return surroundingChunks(point, depth)
				.flatMap(chunk -> chunk.entities())
				.filter(e -> e.isInRange(point, dist));
	}
	
	public Stream<Entity> nearbyEntities(PlayerEntity p) {
		return surroundingChunks(p)
				.flatMap(c -> c.entities())
				.filter(e -> e.isInRange(p, p.getViewingDistance()));
	}
	
	@Override
	public Stream<Entity> nearbyEntities(HasLocation l) {
		Utils.assertArg(!contains(l), "Cannot get nearby Entity Stream from location in another world.");
		return surroundingChunks(l)
				.flatMap(Chunk::entities);
	}
	
	@Override
	public Stream<Entity> nearbyEntities(HasLocation l, double distance) {
		Utils.assertArg(!contains(l), "Cannot get nearby Entity Stream from location in another world.");
		return nearbyEntities(l.getLocation(), distance);
	}
	
	@Override
	protected Stream<Player> doNearbyPlayers(Location point) {
		return surroundingChunks(point)
				.flatMap(Chunk::players);
	}
	
	@Override
	public boolean doRegister(Entity e) {
		if(isRegistered(e)) return false;
		
		//If entity is player, add/setup the player.
		if(Player.is(e))
			initPlayer((PlayerEntity) e);
		
		//Add entity to chunk
		chunk(e).generate().addEntity(e);
		
		//Add entity to world-wide collection.
		entities.put(e.getID(), e);
		return true;
	}
	
	public final boolean isRegistered(final Entity e) {
		return entities.containsKey(e.getID());
	}
	
	private void addPlayer(Player player) {
		players.put(player.getID(), player);
	}
	
	private void removePlayer(Player player) {
		players.remove(player.getID());
	}
	
	private void initPlayer(PlayerEntity pE) {
		Player player = pE.getPlayer();
		addPlayer(player);
		player.sendPacket(EntityPacket.constructCreate(pE));
		
		view(pE).forEach(t -> sendCreateTile(player, t));
		nearbyEntities(pE)
		.forEach(e -> sendCreateEntity(e, player));
		
	}
	
	private Stream<? extends Tile> view(PlayerEntity p) {
		return nearbyTiles(p, p.getViewingDistance());
	}
	
	private Collection<Tile> getView(HasLocation l, double distance) {
		return nearbyTiles(l, distance)
				.collect(Collectors.toSet());
	}
	
	private void sendEntireChunk(Chunk chunk, Player player) {
		chunk.generate();
		sendCreateChunk(chunk,player);
		chunk.entities()
		.filter(e -> !e.equals(player.getEntity()))
		.forEach(e -> sendCreateEntity(e, player));
	}
	
	private void deSendEntireChunk(Chunk chunk, Player player) {
		sendDeleteChunk(chunk, player);
	}
	
	@Override
	public boolean isRegistered(Registerable r) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean register(Registerable r) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean deRegister(Registerable r) {
		if(r instanceof Entity) {
			doDeRegister((Entity)r);
		}
		return false;
	}
	
	@Override
	public boolean doDeRegister(Entity e) {
		if(entities.remove(e.getID()) == null)
			return false;
		final Chunk chunk = ((CLocation)e.getLocation()).getChunk();
		synchronized(chunk) {
			chunk.removeEntity(e);
			if(Player.is(e)) {
				PlayerEntity pE = (PlayerEntity) e;
				chunk.removePlayer(pE.getPlayer());
				removePlayer(pE.getPlayer());
			}
		}
		nearbyPlayers(e).forEach(p -> p.sendPacket(EntityPacket.constructDestroy(e)));
		return true;
	}
	
	@Override
	public Stream<PollableRegion> regions(HasLocation l) {
		if(!contains(l)) throw new IllegalArgumentException("Cannot get Regions from a Location from another World.");
		return chunk(l).regions()
				.filter(s -> s.contains(l));
	}
	
	@Override
	public Entity getEntity(int id) {
		return entities.get(id);
	}
	
	@Override
	public Stream<Entity> entities() {
		return entities.values().stream();
	}
	
	@Override
	public Stream<Entity> entities(HasLocation l) {
		return chunk(l)
				.entities()
				.filter(l::isAt);
	}
	
	public final boolean isRegistered(PollableRegion r) {
		return registeredRegions.contains(r);
	}
	
	@Override
	public void doRegister(PollableRegion r) {
		Stream<Chunk> stream = chunks(r);
		
		if(Regions.isStaticRegion(r)) {
			Collection<Chunk> cachedChunks = new HashSet<>();
			cachedRegionChunks.put(r, cachedChunks);
			stream = stream.peek(cachedChunks::add);
		}
		
		stream.forEach(chunk -> chunk.addRegion(r));
		registeredRegions.add(r);
	}
	
	@Override
	protected void doUnRegister(PollableRegion r) {
		cachedRegionChunks.remove(r);
		registeredRegions.remove(r);
		chunks(r)
		.forEach(chunk -> chunk.removeRegion(r));
	}
	
	@Override
	public Stream<PollableRegion> regions() {
		return registeredRegions.stream();
	}
	
	@Override
	protected void handleRegionChange(PollableRegion region) {
		
	}
	
	protected Stream<Entity> doEntities(final Location point) {
		return chunk(point).entities()
				.filter(point::isAt);
	}
	
	@Override
	protected Stream<Entity> doEntitiesIn(final PollableRegion r) {
		return chunks(r)
				.flatMap(Chunk::entities)
				.filter(r::contains);
	}
	
	private CLocation cLoc(Location loc) {
		if(loc instanceof CLocation)
			return (CLocation) loc;
		return new CLocation(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	protected World doMoveEntity(Entity entity, Location newLocation, MoveType type) {
		CLocation newLoc = cLoc(newLocation);
		CLocation currentLoc = cLoc(entity.getLocation());
		
		final Chunk oldChunk = currentLoc.getChunk();
		final Chunk newChunk = newLoc.getChunk();
		
		if(!oldChunk.equals(newChunk)) {
			newChunk.generate();
			
			oldChunk.removeEntity(entity);
			final Collection<Player> oldPlayers = getNearbyPlayers(currentLoc);
			final Collection<Player> newPlayers = getNearbyPlayers(newLoc);
			newChunk.addEntity(entity);
			
			final EntityPacket destroyPacket = EntityPacket.constructDestroy(entity);
			final EntityPacket createPacket = EntityPacket.constructCreate(entity);
			
			
			
			if(entity instanceof PlayerEntity) {
				PlayerEntity pE = (PlayerEntity) entity;
				Player player = pE.getPlayer();
				int dx = newChunk.getCoordinate().getX() - oldChunk.getCoordinate().getX();
				int dy = newChunk.getCoordinate().getY() - oldChunk.getCoordinate().getY();
				
				for(Player oP : oldPlayers) {
					if(newPlayers.contains(oP)) continue;
					oP.sendPacket(destroyPacket);
					player.sendPacket(EntityPacket.constructDestroy(oP.getEntity()));
				}
				
				for(Player nP : newPlayers) {
					if(oldPlayers.contains(nP)) continue;
					nP.sendPacket(createPacket);
					player.sendPacket(EntityPacket.constructCreate(nP.getEntity()));
				}
				
				/*if(dx != 0) {
					for(int y=-1; y<2; ++y) {
						sendDeleteChunk(checkGenerateChunk(shiftChunk(currentLoc, dx*-1, y)), player);
						sendEntireChunk(shiftChunk(newLoc, dx, y), player);
					}
				}
				if(dy != 0) {
					for(int x=-1; x<2; ++x) {
						sendDeleteChunk(checkGenerateChunk(shiftChunk(currentLoc, x, dy*-1)), player);
						sendEntireChunk(shiftChunk(newLoc, x, dy), player);
					}
				}*/
				
			} else {
				for(Player oP : oldPlayers) {
					if(newPlayers.contains(oP)) continue;
					oP.sendPacket(destroyPacket);
				}
				
				for(Player nP : newPlayers) {
					if(oldPlayers.contains(nP)) continue;
					nP.sendPacket(createPacket);
				}
			}
		}
		
		if(Player.is(entity)) {
			PlayerEntity pE = (PlayerEntity) entity;
			Player player = pE.getPlayer();
			Collection<Tile> newTiles = getView(newLoc, pE.getViewingDistance());
			Collection<Tile> oldTiles = getView(currentLoc, pE.getViewingDistance());
			
			Iterator<Tile> ni = newTiles.iterator();
			while(ni.hasNext()) {
				Tile t = ni.next();
				if(oldTiles.contains(t))
					oldTiles.remove(t);
				else
					sendCreateTile(player, t);
			}
			
			oldTiles.forEach(t -> sendDestroyTile(player, t));
		}
		
		entity.setLocation(newLocation);
		return this;
	}
	
	private static void sendCreateTile(Player player, Tile tile) {
		player.sendPacket(TilePacket.constructCreate(tile));
	}
	
	private static void sendDestroyTile(Player player, Tile tile) {
		player.sendPacket(TilePacket.constructDestroy(tile));
	}
	
	private static void sendCreateChunk(final Chunk chunk, final Player player) {
		player.sendPacket(TilePacket.constructCreate(chunk.tiles));
	}
	
	private static void sendDeleteChunk(final Chunk chunk, final Player player) {
		player.sendPacket(TilePacket.constructDestroy(chunk.tiles));
	}
	
	protected StaticChunkWorld(final String name, final int chunkSize, final int chunkDepth, final WorldGenerator generator) {
		super(name, generator);
		this.chunkSize = chunkSize;
		this.chunkDepth = chunkDepth;
	}
	
	protected StaticChunkWorld(final String name, final int chunkSize, final WorldGenerator generator) {
		this(name, chunkSize, DEFAULT_CHUNK_DEPTH, generator);
	}
	
	protected StaticChunkWorld(final String name, final WorldGenerator generator) {
		this(name, DEFAULT_CHUNK_SIZE, generator);
	}
	
}
