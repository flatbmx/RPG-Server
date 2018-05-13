package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.Utils;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.TilePacket;

public final class StaticChunkWorld extends World {
	
	private static final int DEFAULT_CHUNK_SIZE = 25;
	private static final int DEFAULT_CHUNK_DEPTH = 1;
	
	private final Map<Integer,Player> players = new HashMap<>();
	private final Collection<Player> safePlayers = Collections.unmodifiableCollection(players.values());
	private final Map<Integer,Entity> entities = new HashMap<>();
	private final Map<Integer,Map<ChunkCoordinate,Chunk>> chunks = new HashMap<>();
	
	private final Collection<PollableRegion> registeredRegions = new HashSet<>();
	
	private final int chunkSize;
	private final int chunkDepth;
	
	private final static class ChunkCoordinate {
		
		private final int x,y,z;
		private final int hash;
		
		public ChunkCoordinate move(int dx, int dy, int dz) {
			return new ChunkCoordinate(x + dx, y + dy, z + dz);
		}
		
		public ChunkCoordinate move(int dx, int dy) {
			return move(dx, dy, 0);
		}
		
		@Override
		public String toString() {
			return "ChunkCoord - " + x + ", " + y + ", " + z;
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
				return x == other.x && y == other.y && z == other.z;
			}
			return false;
		}
		
		private final int computeHash() {
			return 79254 + x*37 + y*78 + z*113;
		}
		
		protected ChunkCoordinate(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			hash = computeHash();
		}
		
	}

	private final class Chunk {
		
		private final ChunkCoordinate coord;
		private final SLocation topLeft;
		
		boolean generated = false;

		private final Tile[][] tiles = new Tile[getChunkSize()][getChunkSize()];
		private final Map<Integer,Player> players = new HashMap<>();
		private final Map<Integer,Entity> entities = new HashMap<>();
		private final Set<Region> regions = new HashSet<>(),
				safeRegions = Collections.unmodifiableSet(regions);
		
		@Override
		public String toString() {
			return "Chunk " + getCoordinate();
		}
		
		ChunkCoordinate getCoordinate() {
			return coord;
		}
		
		final int size() {
			return StaticChunkWorld.this.chunkSize;
		}
		
		Stream<Region> regions() {
			return safeRegions.stream();
		}
		
		boolean addRegion(Region r) {
			return regions.add(r);
		}
		
		boolean removeRegion(Region r) {
			return regions.remove(r);
		}
		
		Collection<Entity> getEntities() {
			return entities.values();
		}
		
		Stream<Entity> entities() {
			return getEntities().stream();
		}
		
		Stream<Player> players() {
			return players.values().stream();
		}
		
		StaticChunkWorld getWorld() {
			return StaticChunkWorld.this;
		}
		
		Tile getTile(SLocation point) {
			return tiles[point.x - topLeft.x][point.y - topLeft.y];
		}
		
		Tile getTile(int x, int y) {
			return tiles[x][y];
		}
		
		void setTile(Tile newTile, int x, int y) {
			tiles[x][y] = newTile;
		}
		
		void addEntity(Entity entity) {
			if(entity instanceof PlayerEntity) {
				PlayerEntity pE = (PlayerEntity) entity;
				players.put(pE.getPlayer().getID(), pE.getPlayer());
			}
				
			entities.put(entity.getID(), entity);
		}
		
		void removeEntity(Entity entity) {
			if(entity instanceof PlayerEntity) {
				Player player = ((PlayerEntity)entity).getPlayer();
				removePlayer(player);
			}
			entities.remove(entity.getID());
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
		
		final Stream<Tile> tiles() {
			return Arrays.stream(tiles)
				.flatMap(Arrays::stream);
		}
		
		protected Chunk(ChunkCoordinate coord) {
			this.coord = coord;
			int x = coord.x * getChunkSize() - (getChunkSize()-1)/2;
			int y = coord.y * getChunkSize() - (getChunkSize()-1)/2;
			topLeft = new SLocation(this, x, y, coord.z);
		}

	}

	private final class MLocation extends Location {

		private int x, y, z;

		@Override
		public World getWorld() {
			return StaticChunkWorld.this;
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public int getZ() {
			return z;
		}

		@Override
		public Location move(int dx, int dy, int dz) {
			return new MLocation(x + dx, y + dy, z + dz);
		}

		MLocation(int nx, int ny, int nz) {
			x = nx;
			y = ny;
			z = nz;
		}

	}

	private final class SLocation extends Location {

		private Chunk chunk;
		private final int x, y, z;
		private final int hash;

		@Override
		public World getWorld() {
			return StaticChunkWorld.this;
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public int getZ() {
			return z;
		}
		
		@Override
		public final String toString() {
			return x + ", " + y + ", " + z;
		}
		
		@Override
		public SLocation move(final int dx, final int dy, final int dz) {
			final SLocation sl = getChunk().topLeft;
			final int nX = x + dx, nY = y + dy;
			if(nX < sl.x || nY < sl.y || nX - sl.x >= getChunkSize() || nY - sl.y >= getChunkSize())
				return new SLocation(nX, nY, z + dz);
			return new SLocation(chunk, nX, nY, z + dz);
		}
		
		public SLocation move(final int dx, final int dy) {
			return move(dx, dy, 0);
		}
		
		@Override
		public final int hashCode() {
			return hash;
		}
		
		@Override
		public final boolean equals(Object o) {
			if(this == o) return true;
			if(o == null) return false;
			if(!(o instanceof SLocation)) return false;
			SLocation other = (SLocation) o;
			return getX() == other.getX() && getY() == other.getY() && getZ() == other.getZ();
		}
		
		@Override
		public final SLocation clone() {
			return new SLocation(chunk, x, y, z);
		}
		
		Chunk getChunk() {
			if(chunk == null) {
				chunk = StaticChunkWorld.this.findChunk(this);
			}
			return chunk;
		}
		
		protected SLocation(Chunk chunk, int x, int y, int z) {
			this.chunk = chunk;
			this.x = x;
			this.y = y;
			this.z = z;
			hash = 79254 * 31 + x*17 + y*77 + z*111;
		}

		protected SLocation(int x, int y, int z) {
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
		return chunk((SLocation) l.getLocation());
	}
	
	private Chunk chunk(SLocation point) {
		return point.getChunk();
	}
	
	public Stream<Chunk> chunks() {
		return chunks.values().stream()
				.flatMap(map -> map.values().stream());
	}
	
	public Stream<Chunk> chunks(int z) {
		Map<ChunkCoordinate,Chunk> c = chunks.get(z);
		if(c == null)
			return Stream.empty();
		return c.values().stream();
	}
	
	@Override
	public Stream<Tile> tiles() {
		return chunks()
				.flatMap(Chunk::tiles);
	}
	
	@Override
	public Stream<Tile> tiles(int z) {
		return chunks(z)
				.flatMap(Chunk::tiles);
	}
	
	public Stream<Tile> tiles(PollableRegion r) {
		Objects.requireNonNull(r, "Cannot find tiles in null region!");
		return r.points()
				.map(point -> getTile(point));
	}
	
	@Override
	public Stream<Tile> nearbyTiles(Locatable l) {
		return surroundingChunks(l)
				.flatMap(Chunk::tiles);
	}
	
	@Override
	public Stream<Tile> nearbyTiles(Locatable l, double distance) {
		int depth = (int)Math.floor(distance / getChunkSize());
		return surroundingChunks(l, depth)
				.flatMap(Chunk::tiles);
	}
	
	/**
	 * Returns a stream of chunks that the passed region is in.
	 * @param r - The region that's chunks should be returned.
	 * @return Stream of chunks that the passed region is in.
	 */
	private Stream<Chunk> chunks(PollableRegion r) {
		//TODO if static region, have the chunks of the regions cached.
		return r.points()
				.map(this::chunk)
				.distinct();
	}
	
	private Stream<Chunk> surroundingChunks(SLocation point, int depth) {
		ChunkCoordinate center = getCoordinate(point);
		return IntStream.range(-depth, depth+1)
			.mapToObj(Integer::valueOf)
			.flatMap(i -> {
				return IntStream.range(-depth, depth+1)
						.mapToObj(j -> findChunk(center.move(j, i)));
			});
	}
	
	private Stream<Chunk> surroundingChunks(Locatable l, int depth) {
		return surroundingChunks((SLocation)l.getLocation(), depth);
	}
	
	private Stream<Chunk> surroundingChunks(Locatable l) {
		return surroundingChunks((SLocation)l.getLocation());
	}
	
	private Stream<Chunk> surroundingChunks(SLocation point) {
		return surroundingChunks(point, getChunkDepth());
	}
	
	private Chunk findChunk(final SLocation point) {
		return findChunk(getCoordinate(point));
	}
	
	private Chunk findChunk(final ChunkCoordinate coord) {
		Map<ChunkCoordinate,Chunk> ch = chunks.get(coord.z);
		if(ch == null) {
			ch = new HashMap<ChunkCoordinate,Chunk>();
			chunks.put(coord.z, ch);
		}
		Chunk chunk = ch.get(coord);
		if(chunk == null) {
			chunk = new Chunk(coord);
			ch.put(coord, chunk);
		}
		return chunk;
	}
	
	private Chunk getGeneratedChunk(final ChunkCoordinate coord) {
		return checkGenerateChunk(findChunk(coord));
	}
	
	private Chunk getGeneratedChunk(final Locatable point) {
		return getGeneratedChunk(getCoordinate(point.getLocation()));
	}
	
	private ChunkCoordinate getCoordinate(final Location point) {
		//if(point instanceof SLocation) return ((SLocation)point).getChunk().coord;
		return getCoordinateFromLocation(point.getX(), point.getY(), point.getZ());
	}

	private ChunkCoordinate getCoordinateFromLocation(final int x, final int y, final int z) {
		int cx = (int) Math.floor((x+(getChunkSize()-1)/2d)/getChunkSize());
		int cy = (int) Math.floor((y+(getChunkSize()-1)/2d)/getChunkSize());
		return new ChunkCoordinate(cx,cy,z);
	}
	
	/**
	 * If the given chunk is not generated generate it.
	 * @param chunk - The chunk that should be generated
	 * @return The chunk for chaining
	 */
	private final Chunk checkGenerateChunk(final Chunk chunk) {
		if(!chunk.isGenerated()) generateChunk(chunk);
		return chunk;
	}
	
	/**
	 * Generates the given chunk using this worlds world generator.
	 * <b>NOTE:</b> This will re-generate already generated chunks.
	 * @param chunk
	 */
	private void generateChunk(final Chunk chunk) {
		getWorldGenerator().doGenerateRectTiles(chunk.tiles, chunk.topLeft);
		chunk.generated = true;
	}
	
	private Chunk shiftChunk(Chunk chunk, int dx, int dy, int dz) {
		return findChunk(chunk.getCoordinate().move(dx, dy, dz));
	}
	
	private Chunk shiftChunk(Chunk chunk, int dx, int dy) {
		return shiftChunk(chunk, dx, dy, 0);
	}
	
	private Chunk shiftChunk(SLocation point, int dx, int dy, int dz) {
		return shiftChunk(chunk(point), dx, dy, dz);
	}
	
	private Chunk shiftChunk(SLocation point, int dx, int dy) {
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
		SLocation tL = (SLocation) topLeft;
		Chunk topLeftChunk = tL.getChunk();
		Chunk topRightChunk = tL.move(tiles.length, 0).getChunk();
		int chunkWidth = topRightChunk.getCoordinate().x - topLeftChunk.getCoordinate().x;
		Chunk bottomLeftChunk = tL.move(0, tiles[0].length, 0).getChunk();
		int chunkHeight = bottomLeftChunk.coord.y - topLeftChunk.coord.y;
		final int width = tiles.length, height = tiles[0].length;
		MLocation loc = new MLocation(tL.x,tL.y, topLeft.getZ());
		for(int i=0; i<=chunkWidth; ++i) {
			for(int j=0; j<chunkHeight; ++j) {
				Chunk chunk = getGeneratedChunk(topLeftChunk.coord.move(i,j,0));
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
		checkGenerateChunk(chunk(e)).addEntity(e);
		
		//If entity is player, add/setup the player.
		if(e.isPlayer()) initPlayer((PlayerEntity) e);
		
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
		
		surroundingChunks(pE)
			.forEach(chunk -> sendEntireChunk(chunk, player));
	}
	
	private void sendEntireChunk(Chunk chunk, Player player) {
		sendCreateChunk(player,chunk);
		chunk.entities()
			.filter(e -> !e.equals(player.getEntity()))
			.forEach(e -> sendCreateEntity(e, player));
	}
	
	private void deSendEntireChunk(Chunk chunk, Player player) {
		sendDeleteChunk(player, chunk);
	}
	
	@Override
	public boolean doDeRegister(Entity e) {
		if(entities.remove(e.getID()) == null) return false;
		final Chunk chunk = ((SLocation)e.getLocation()).getChunk();
		synchronized(chunk) {
			chunk.removeEntity(e);
			if(e.isPlayer()) {
				PlayerEntity pE = (PlayerEntity) e;
				chunk.removePlayer(pE.getPlayer());
				removePlayer(pE.getPlayer());
			}
		}
		return true;
	}
	
	@Override
	public Stream<Region> regionsAt(Locatable loc) {
		if(!contains(loc)) throw new IllegalArgumentException("Cannot get Regions from a Location from another World.");
		return ((SLocation)loc.getLocation()).getChunk().regions()
				.filter(s -> s.contains(loc));
	}
	
	@Override
	public Entity getEntity(int id) {
		return entities.get(id);
	}
	
	@Override
	public Stream<Entity> entities() {
		return entities.values().stream();
	}
	
	public Stream<Entity> entities(Locatable l) {
		return chunk(l).entities()
				.filter(e -> e.isAt(l));
	}
	
 	public final boolean isRegistered(PollableRegion r) {
		return registeredRegions.contains(r);
	}
	
	@Override
	public void doRegister(PollableRegion r) {
		chunks(r)
		.forEach(chunk -> chunk.addRegion(r));
		
		registeredRegions.add(r);
	}
	
	@Override
	public StaticChunkWorld deRegister(PollableRegion r) {
		chunks(r)
		.forEach(chunk -> chunk.removeRegion(r));
		
		registeredRegions.remove(r);
		return this;
	}
	
	public Stream<Entity> entitiesAtLocation(final Location point) {
		if(!contains(point)) throw new IllegalArgumentException("Cannot get Entities at a Location from another World.");
		return chunk(point).entities()
				.filter(e -> e.isAt(point));
	}
	
	@Override
	protected Stream<Entity> doEntitiesIn(final PollableRegion r) {
		return chunks(r)
				.flatMap(Chunk::entities)
				.filter(r::contains);
	}
	
	@Override
	public SLocation createLocation(final int x, final int y, final int z) {
		return new SLocation(x, y, z);
	}
	
	@Override
	protected World doMoveEntity(Entity entity, Location newLocation, MoveType type) {
		SLocation newLoc = (SLocation) newLocation;
		SLocation currentLoc = (SLocation) entity.getLocation();

		final Chunk oldChunk = currentLoc.getChunk();
		final Chunk newChunk = newLoc.getChunk();

		if(!oldChunk.equals(newChunk)) {
			checkGenerateChunk(newChunk);
			
			oldChunk.removeEntity(entity);
			final Collection<Player> oldPlayers = getNearbyPlayers(currentLoc);
			final Collection<Player> newPlayers = getNearbyPlayers(newLoc);
			newChunk.addEntity(entity);
			
			final EntityPacket destroyPacket = EntityPacket.constructDestroy(entity);
			final EntityPacket createPacket = EntityPacket.constructCreate(entity);
			
			if(entity instanceof PlayerEntity) {
				PlayerEntity pE = (PlayerEntity) entity;
				Player player = pE.getPlayer();
				oldChunk.removePlayer(pE.getPlayer());
				newChunk.addPlayer(pE.getPlayer());
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
				
				if(dx != 0) {
					for(int y=-1; y<2; ++y) {
						sendDeleteChunk(player, checkGenerateChunk(shiftChunk(currentLoc, dx*-1, y)));
						sendCreateChunk(player, checkGenerateChunk(shiftChunk(currentLoc, dx, y)));
					}
				}
				if(dy != 0) {
					for(int x=-1; x<2; ++x) {
						sendDeleteChunk(player, checkGenerateChunk(shiftChunk(currentLoc, x, dy*-1)));
						sendCreateChunk(player, checkGenerateChunk(shiftChunk(currentLoc, x, dy)));
					}
				}
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
		entity.setLocation(newLocation);
		return this;
	}
	
	private static void sendCreateChunk(final Player player, final Chunk chunk) {
		player.sendPacket(TilePacket.constructCreate(chunk.tiles));
	}
	
	private static void sendDeleteChunk(final Player player, final Chunk chunk) {
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
