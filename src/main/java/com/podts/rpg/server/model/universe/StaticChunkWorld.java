package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;

public final class StaticChunkWorld extends World {
	
	private static final int CHUNK_SIZE = 17;
	
	private final Map<Integer,Map<ChunkCoordinate,Chunk>> chunks = new HashMap<>();
	
	private static class ChunkCoordinate {
		private int x,y,z;
		protected ChunkCoordinate(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	private class Chunk {
		
		private final ChunkCoordinate coord;
		private final SLocation topLeft;
		
		private final Tile[][] tiles = new Tile[CHUNK_SIZE][CHUNK_SIZE];
		private final Map<Integer,Entity> entities = new HashMap<>();
		private final Set<Region> regions = new HashSet<>();
		
		protected Chunk(ChunkCoordinate coord) {
			this.coord = coord;
			int x = coord.x * CHUNK_SIZE + CHUNK_SIZE/2;
			int y = coord.y * CHUNK_SIZE + CHUNK_SIZE/2;
			topLeft = new SLocation(this, x, y, coord.z);
		}
		
	}
	
	private final class SLocation extends Location {
		
		private final Chunk chunk;
		private final int x, y, z;
		
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
		public SLocation move(int dx, int dy, int dz) {
			SLocation sl = chunk.topLeft;
			int nX = x + dx, nY = y + dy;
			if(nX < sl.x || nY < sl.y || nX - sl.x >= CHUNK_SIZE || nY - sl.y >= CHUNK_SIZE)
				return new SLocation(nX, nY, z + dz);
			return new SLocation(chunk, nX, nY, z + dz);
		}
		
		protected SLocation(Chunk chunk, int x, int y, int z) {
			this.chunk = chunk;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		protected SLocation(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			chunk = getOrGenerateChunkFromLocation(this);
		}
		
	}
	
	private Set<Chunk> getChunksFromCoordinates(Collection<ChunkCoordinate> coords) {
		final Set<Chunk> result = new HashSet<>();
		for(ChunkCoordinate coord : coords) {
			result.add(getOrGenerateChunk(coord));
		}
		return result;
	}
	
	private Set<ChunkCoordinate> getCoordinatesFromLocations(Collection<Location> points) {
		final Set<ChunkCoordinate> coords = new HashSet<ChunkCoordinate>();
		for(Location point : points) {
			coords.add(getCoordinateFromLocation(point));
		}
		return coords;
	}
	
	private Chunk getOrGenerateChunkFromLocation(Location point) {
		return getOrGenerateChunk(getCoordinateFromLocation(point));
	}
	
	private Chunk getOrGenerateChunk(ChunkCoordinate coord) {
		Map<ChunkCoordinate,Chunk> ch = chunks.get(coord.z);
		Chunk chunk = ch.get(coord);
		if(chunk == null) {
			chunk = new Chunk(coord);
			getWorldGenerator().generateRectTiles(chunk.tiles, chunk.topLeft.getX(), chunk.topLeft.getY(), chunk.topLeft.getZ());
		}
		return chunk;
	}
	
	private ChunkCoordinate getCoordinateFromLocation(Location point) {
		int cx = (point.getX()-CHUNK_SIZE/2)/CHUNK_SIZE;
		int cy = (point.getY()-CHUNK_SIZE/2)/CHUNK_SIZE;
		return new ChunkCoordinate(cx,cy,point.getZ());
	}
	
	@Override
	public Tile getTile(Locatable loc) {
		if(loc == null) throw new IllegalArgumentException("Cannot get Tile for null location.");
		final Location point = loc.getLocation();
		if(!equals(point.getWorld())) throw new IllegalArgumentException("Cannot get Tile that exists in a different World.");
		
		Chunk chunk = getOrGenerateChunk(getCoordinateFromLocation(point));
		Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		synchronized(chunk) {
			return chunk.tiles[x][y];
		}
	}
	
	public StaticChunkWorld setTile(Tile newTile, Location point) {
		if(newTile == null) throw new IllegalArgumentException("Cannot set a Tile as null.");
		if(point == null) throw new IllegalArgumentException("Cannot set a Tile at a null location.");
		if(!equals(point.getWorld())) throw new IllegalArgumentException("Cannot set a Tile that exists in another World.");
		
		Chunk chunk = getOrGenerateChunk(getCoordinateFromLocation(point));
		Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		synchronized(chunk) {
			chunk.tiles[x][y] = newTile;
		}
		return this;
	}
	
	@Override
	public Collection<Entity> getNearbyEntities(Locatable l, double distance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Collection<Entity> getNearbyEntities(Locatable l) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean register(Entity e) {
		Chunk chunk = getOrGenerateChunkFromLocation(e.getLocation());
		synchronized(chunk) {
			chunk.entities.put(e.getID(), e);
		}
		return true;
	}
	
	@Override
	public World deRegister(Entity e) {
		Chunk chunk = getOrGenerateChunkFromLocation(e.getLocation());
		synchronized(chunk) {
			chunk.entities.remove(e.getID());
		}
		return this;
	}
	
	@Override
	public Collection<Region> getRegionsAtLocation(Locatable loc) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected StaticChunkWorld(String name, WorldGenerator generator) {
		super(name, generator);
	}

	@Override
	public Entity getEntity(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StaticChunkWorld registerRegion(PollableRegion r) {
		Set<Chunk> chunks = getChunksFromCoordinates(getCoordinatesFromLocations(r.getPoints()));
		for(Chunk c : chunks) {
			c.regions.add(r);
		}
		return this;
	}

	@Override
	public StaticChunkWorld deRegisterRegion(PollableRegion r) {
		Set<Chunk> chunks = getChunksFromCoordinates(getCoordinatesFromLocations(r.getPoints()));
		for(Chunk c : chunks) {
			c.regions.remove(r);
		}
		return this;
	}

	@Override
	public Collection<Entity> getEntitiesInRegion(PollableRegion r) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public SLocation createLocation(int x, int y, int z) {
		return new SLocation(x, y, z);
	}
	
}
