package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.Utils;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.MoveType;
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
	
	private ChunkPlane bottom, top;
	
	@Override
	public ChunkPlane getTopPlane() {
		return top;
	}
	
	@Override
	public ChunkPlane getBottomPlane() {
		return bottom;
	}
	
	private final static class ChunkCoordinate {
		
		private final int x,y,z;
		private final int hash;
		
		public ChunkCoordinate shift(int dx, int dy, int dz) {
			return new ChunkCoordinate(x + dx, y + dy, z + dz);
		}
		
		public ChunkCoordinate shift(int dx, int dy) {
			return shift(dx, dy, 0);
		}
		
		@Override
		public String toString() {
			return "[ChkCrd : " + x + ", " + y + ", " + z + "]";
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
				return x == other.x
						&& y == other.y
						&& z == other.z;
			}
			return false;
		}
		
		protected ChunkCoordinate(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			hash = Objects.hash(x, y, z);
		}
		
	}

	private final class Chunk extends IncompleteRegion implements RectangularRegion, PollableMonitoringRegion, HasPlane {
		
		private final ChunkPlane plane;
		private final ChunkCoordinate coord;
		private final CLocation topLeft;
		
		boolean generated = false;

		private final Tile[][] tiles = new Tile[getChunkSize()][getChunkSize()];
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
		public String toString() {
			return "Chunk " + getCoordinate();
		}
		
		int chunkSize() {
			this.getCorners();
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
		
		@Override
		public ChunkPlane getPlane() {
			return plane;
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
		
		StaticChunkWorld getWorld() {
			return StaticChunkWorld.this;
		}
		
		@Override
		public Collection<Location> getPoints() {
			return points()
					.collect(Collectors.toSet());
		}
		
		@Override
		public Stream<? extends Location> points() {
			if(!isGenerated()) {
				return generatePoints();
			}
			return tiles()
					.map(Tile::getLocation);
		}
		
		Stream<CLocation> generatePoints() {
			return IntStream.range(0, chunkSize())
					.mapToObj(Integer::valueOf)
					.flatMap(j -> {
						return IntStream.range(0, chunkSize())
								.mapToObj(i -> new CLocation(this, topLeft.x + i, topLeft.y + j, getZ()));
					});
		}
		
		@Override
		public final Stream<Tile> tiles() {
			if(!isGenerated()) return Stream.empty();
			return Arrays.stream(tiles)
				.flatMap(Arrays::stream)
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
		
		Tile getTile(CLocation point) {
			return tiles[point.x - topLeft.x][point.y - topLeft.y];
		}
		
		Tile getTile(int x, int y) {
			return tiles[x][y];
		}
		
		void setTile(Tile newTile, int x, int y) {
			tiles[x][y] = newTile;
		}
		
		@Override
		public Chunk addEntity(Entity entity) {
			if(entity instanceof PlayerEntity) {
				PlayerEntity pE = (PlayerEntity) entity;
				addPlayer(pE.getPlayer());
			}
			entities.put(entity.getID(), entity);
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
		public boolean contains(Locatable point) {
			//TODO Should probably do mathematical comparison.
			CLocation loc = (CLocation) point.getLocation();
			return equals(loc.getChunk());
		}
		
		protected Chunk(final ChunkPlane plane, final ChunkCoordinate coord) {
			this.plane = plane;
			this.coord = coord;
			int x = coord.x * getChunkSize() - (getChunkSize()-1)/2;
			int y = coord.y * getChunkSize() - (getChunkSize()-1)/2;
			topLeft = new CLocation(this, x, y, coord.z);
		}

		@Override
		public List<CLocation> getCorners() {
			return corners()
					.collect(Collectors.toList());
		}
		
		@Override
		public Stream<CLocation> corners() {
			return Corner.stream()
					.map(this::getCorner);
		}
		
		@Override
		public CLocation getCorner(Corner c) {
			if(Corner.TOP_LEFT.equals(c)) return topLeft;
			return new CLocation(this, topLeft.getX() + chunkSize()*c.getX(), topLeft.getY() + chunkSize()*c.getY(), getZ());
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
			return equals(getTopPlane());
		}
		
		@Override
		public boolean isBottom() {
			return equals(getBottomPlane());
		}
		
		Chunk getOrCreateChunk(final ChunkCoordinate coord) {
			return chunks.computeIfAbsent(coord, (c) -> new Chunk(this, c));
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
	
	private final class MLocation extends Location {

		private int x, y, z;
		
		@Override
		public final StaticChunkWorld getSpace() {
			return StaticChunkWorld.this;
		}
		
		@Override
		public final ChunkPlane getPlane() {
			return getSpace().getPlane(getZ());
		}
		
		@Override
		public final int getX() {
			return x;
		}

		@Override
		public final int getY() {
			return y;
		}

		@Override
		public final int getZ() {
			return z;
		}

		@Override
		public final MLocation shift(final int dx, final int dy, final int dz) {
			return new MLocation(x + dx, y + dy, z + dz);
		}

		private MLocation(int nx, int ny, int nz) {
			x = nx;
			y = ny;
			z = nz;
		}

	}

	private final class CLocation extends SimpleLocation {

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
		
		@Override
		public final String toString() {
			return x + ", " + y + ", " + z;
		}
		
		@Override
		public final CLocation shift(final int dx, final int dy, final int dz) {
			final CLocation sl = getChunk().topLeft;
			final int nX = x + dx, nY = y + dy, nZ = z + dz;
			Chunk c = chunk;
			if(nZ != z || nX < sl.x || nY < sl.y || nX - sl.x >= getChunkSize() || nY - sl.y >= getChunkSize())
				c = null;
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
			return new CLocation(chunk, x, y, z);
		}
		
		final Chunk getChunk() {
			if(chunk == null) {
				chunk = StaticChunkWorld.this.findChunk(this);
			}
			return chunk;
		}
		
		private CLocation(final Chunk chunk, final int x, final int y, final int z) {
			super(x, y, z);
			this.chunk = chunk;
			hash = Objects.hash(x, y, z);
		}

		private CLocation(final int x, final int y, final int z) {
			this(null, x, y, z);
		}

	}
	
	private int getChunkSize() {
		return chunkSize;
	}
	
	private int getChunkDepth() {
		return chunkDepth;
	}
	
	private Chunk chunk(Locatable l) {
		return chunk((CLocation) l.getLocation());
	}
	
	private Chunk chunk(CLocation point) {
		return point.getChunk();
	}
	
	public Stream<Chunk> chunks() {
		return planes()
				.flatMap(ChunkPlane::chunks);
	}
	
	public Stream<Chunk> generatedChunks() {
		return planes()
				.flatMap(ChunkPlane::generatedChunks);
	}
	
	@Override
	public Collection<ChunkPlane> getPlanes() {
		return safePlanes.values();
	}
	
	@Override
	public Stream<ChunkPlane> planes() {
		return planes.values().stream();
	}
	
	@Override
	public ChunkPlane getPlane(final int z) {
		return planes.get(z);
	}
	
	private ChunkPlane getOrCreatePlane(final int z) {
		ChunkPlane result = planes.computeIfAbsent(z, ChunkPlane::new);
		if(getTopPlane() == null) {
			top = result;
			bottom = result;
		} else if(result.isAbove(getTopPlane())) {
			top = result;
		} else if(result.isBelow(getBottomPlane())) {
			bottom = result;
		}
		return result;
	}
	
	public Stream<Chunk> chunks(final int z) {
		ChunkPlane plane = getPlane(z);
		if(plane == null) return Stream.empty();
		return plane.chunks();
	}
	
	@Override
	public Stream<Tile> tiles() {
		return generatedChunks()
				.flatMap(Chunk::tiles);
	}
	
	@Override
	public Stream<Tile> nearbyTiles(Locatable l) {
		return surroundingChunks(l)
				.flatMap(Chunk::tiles);
	}
	
	@Override
	public Stream<Tile> nearbyTiles(Locatable l, double distance) {
		int depth = (int)Math.ceil(distance / getChunkSize());
		return surroundingChunks(l, depth)
				.peek(Chunk::generate)
				.flatMap(Chunk::tiles)
				.filter(tile -> tile.isInRange(l, distance));
	}
	
	@Override
	public Stream<Tile> nearbyWalkingTiles(Locatable l, int distance) {
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
	 * Returns a stream of non generated chunks that is surrounding the given point.
	 * @param point - The central point
	 * @param depth - the depth of chunks
	 * @return Stream containing the non generated surrounding chunks of the point
	 */
	private Stream<Chunk> surroundingChunks(CLocation point, int depth) {
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
	
	private Stream<Chunk> surroundingChunks(Locatable l, int depth) {
		return surroundingChunks((CLocation)l.getLocation(), depth);
	}
	
	private Stream<Chunk> surroundingChunks(Locatable l) {
		return surroundingChunks((CLocation)l.getLocation());
	}
	
	private Chunk findChunk(final CLocation point) {
		return findChunk(getCoordinate(point));
	}
	
	private Chunk findChunk(final ChunkCoordinate coord) {
		return getOrCreatePlane(coord.z).getOrCreateChunk(coord);
	}
	
	private Chunk getGeneratedChunk(final ChunkCoordinate coord) {
		return findChunk(coord).generate();
	}
	
	private Chunk getGeneratedChunk(final Locatable point) {
		return getGeneratedChunk(getCoordinate(point.getLocation()));
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
	public Tile doGetTile(final Location point) {
		final Chunk chunk = getGeneratedChunk(point);
		final Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		return chunk.getTile(x, y);
	}

	@Override
	protected void doGetTiles(Tile[][] tiles, Location topLeft) {
		CLocation tL = (CLocation) topLeft;
		Chunk topLeftChunk = tL.getChunk();
		Chunk topRightChunk = tL.shift(tiles.length, 0).getChunk();
		int chunkWidth = topRightChunk.getCoordinate().x - topLeftChunk.getCoordinate().x;
		Chunk bottomLeftChunk = tL.shift(0, tiles[0].length, 0).getChunk();
		int chunkHeight = bottomLeftChunk.coord.y - topLeftChunk.coord.y;
		final int width = tiles.length, height = tiles[0].length;
		MLocation loc = new MLocation(tL.x,tL.y, topLeft.getZ());
		for(int i=0; i<=chunkWidth; ++i) {
			for(int j=0; j<chunkHeight; ++j) {
				Chunk chunk = getGeneratedChunk(topLeftChunk.coord.shift(i,j,0));
				for(int dy=loc.getY() - chunk.topLeft.getY(); dy<getChunkSize(); ++dy) {
					for(int dx=loc.getX() - chunk.topLeft.getX(); dx<getChunkSize(); ++dx) {
						//TODO Implement this, holy shit!
					}
				}
			}
		}
	}
	
	@Override
	protected void doSetTile(Tile newTile) {
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
	
	@Override
	public Stream<Entity> nearbyEntities(Locatable l) {
		Utils.assertArg(!contains(l), "Cannot get nearby Entity Stream from location in another world.");
		return surroundingChunks(l)
				.flatMap(Chunk::entities);
	}
	
	@Override
	public Stream<Entity> nearbyEntities(Locatable l, double distance) {
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
		
		//Add entity to chunk
		chunk(e).generate().addEntity(e);
		
		//If entity is player, add/setup the player.
		if(Player.is(e)) initPlayer((PlayerEntity) e);
		
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
		
		//surroundingChunks(pE)
		//.forEach(chunk -> sendEntireChunk(chunk, player));
		
		nearbyTiles(pE, 15)
		.forEach(t -> sendCreateTile(player, t));
		
	}
	
	private Collection<Tile> getView(Locatable l) {
		return nearbyTiles(l, 15)
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
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean doDeRegister(Entity e) {
		if(entities.remove(e.getID()) == null) return false;
		final Chunk chunk = ((CLocation)e.getLocation()).getChunk();
		synchronized(chunk) {
			chunk.removeEntity(e);
			if(Player.is(e)) {
				PlayerEntity pE = (PlayerEntity) e;
				chunk.removePlayer(pE.getPlayer());
				removePlayer(pE.getPlayer());
			}
		}
		return true;
	}
	
	@Override
	public Stream<PollableRegion> regions(Locatable l) {
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
	public Stream<Entity> entities(Locatable l) {
		return chunk(l).entities()
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
	
	@Override
	public CLocation createLocation(final int x, final int y, final int z) {
		return new CLocation(x, y, z);
	}
	
	@Override
	protected World doMoveEntity(Entity entity, Location newLocation, MoveType type) {
		CLocation newLoc = (CLocation) newLocation;
		CLocation currentLoc = (CLocation) entity.getLocation();

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
				int dx = newChunk.getCoordinate().x - oldChunk.getCoordinate().x;
				int dy = newChunk.getCoordinate().y - oldChunk.getCoordinate().y;
				
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
			Collection<Tile> newTiles = getView(newLoc);
			Collection<Tile> oldTiles = getView(currentLoc);
			
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
