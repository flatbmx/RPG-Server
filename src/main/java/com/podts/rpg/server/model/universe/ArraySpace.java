package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.region.PollableRegion;

public class ArraySpace extends Space {
	
	private int width, height;
	
	private final ArrayPlane[] planes;
	private final List<ArrayPlane> planeList;
	
	private final Set<PollableRegion> regions = new HashSet<>(),
			safeRegions = Collections.unmodifiableSet(regions);
	
	private final class ArrayLocation extends SimpleLocation {
		
		@Override
		public final ArraySpace getSpace() {
			return ArraySpace.this;
		}
		
		@Override
		public final ArrayPlane getPlane() {
			return getSpace().getPlane(getZ());
		}
		
		public ArrayLocation(final int x, final int y, final int z) {
			super(x, y, z);
		}
		
	}
	
	private final class ArrayPlane extends Plane {
		
		private final Tile[][] tiles;
		private final Map<Integer,Entity> entities = new HashMap<>(),
				safeEntities = Collections.unmodifiableMap(entities);
		private final Set<PollableRegion> regions = new HashSet<>(),
				safeRegions = Collections.unmodifiableSet(regions);
		
		@Override
		public final ArraySpace getSpace() {
			return ArraySpace.this;
		}
		
		public final int getWidth() {
			return getSpace().width;
		}
		
		public final int getHeight() {
			return getSpace().height;
		}
		
		@Override
		public boolean isTop() {
			return getZ() == getSpace().planes.length - 1;
		}
		
		@Override
		public boolean isBottom() {
			return getZ() == 0;
		}
		
		boolean isInBounds(Spatial l) {
			Location point = l.getLocation();
			return isInBounds(point.getX(), point.getY());
		}
		
		boolean isInBounds(int x, int y) {
			return x >= 0 &&
					x < getWidth() &&
					y >= 0 &&
					y < getHeight();
		}
		
		@Override
		public Collection<Tile> getTiles() {
			return tiles()
					.collect(Collectors.toSet());
		}
		
		public Stream<Tile> allTiles() {
			return Arrays.stream(tiles)
					.flatMap(Arrays::stream);
		}
		
		@Override
		public Stream<Tile> tiles() {
			return allTiles()
					.filter(Tile::isNotVoid);
		}
		
		@Override
		public Tile getTile(Spatial l) {
			if(!contains(l)) throw new IllegalArgumentException("Cannot get Tile from another Space!");
			Location point = l.getLocation();
			if(isInBounds(point)) return new Tile(TileType.VOID, point);
			return tiles[point.getX()][point.getY()];
		}
		
		@Override
		public Collection<Entity> getEntities() {
			return safeEntities.values();
		}
		
		@Override
		public Collection<PollableRegion> getRegions() {
			return safeRegions;
		}
		
		ArrayPlane(final int x, final int y, final int z) {
			super(z);
			this.tiles = new Tile[x][y];
		}
		
	}
	
	@Override
	public final Collection<ArrayPlane> getPlanes() {
		return planeList;
	}
	
	@Override
	public ArrayLocation createLocation(final int x, final int y, final int z) {
		return new ArrayLocation(x, y, z);
	}
	
	@Override
	public final ArrayPlane getTopPlane() {
		return planes[planes.length-1];
	}
	
	@Override
	public final ArrayPlane getBottomPlane() {
		return planes[0];
	}
	
	@Override
	public boolean isRegistered(Registerable r) {
		if(r instanceof PollableRegion) {
			PollableRegion region = (PollableRegion) r;
			return regions.contains(region);
		}
		return false;
	}
	
	@Override
	public boolean register(Registerable r) {
		if(r instanceof Tile) {
			Tile tile = (Tile) r;
			Location point = tile.getLocation();
			if(!isInBounds(point)) return false;
			ArrayPlane plane = getPlane(point.getZ());
			plane.tiles[point.getX()][point.getY()] = tile;
			return true;
		}
		if(r instanceof PollableRegion) {
			PollableRegion region = (PollableRegion) r;
			return regions.add(region);
		}
		return false;
	}
	
	@Override
	public boolean deRegister(Registerable r) {
		if(r instanceof PollableRegion) {
			PollableRegion region = (PollableRegion) r;
			return regions.remove(region);
		}
		return false;
	}
	
	@Override
	public Stream<? extends Plane> planes() {
		return Arrays.stream(planes);
	}
	
	@Override
	public ArrayPlane getPlane(int z) {
		if(z < 0 || z >= planes.length) return null;
		return planes[z];
	}
	
	public Collection<PollableRegion> getRegions() {
		return safeRegions;
	}
	
	@Override
	public Stream<PollableRegion> regions() {
		return regions.stream();
	}
	
	@Override
	Location moveEntity(Entity entity, MoveType update, int dx, int dy, int dz) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected final boolean isInBounds(Location point) {
		return isInBounds(point.getX(), point.getY(), point.getZ());
	}
	
	protected final boolean isInBounds(final int x, final int y, final int z) {
		final ArrayPlane plane = getPlane(z);
		if(plane == null) return false;
		return x >= 0 &&
				x < plane.getWidth() &&
				y >= 0 &&
				y < plane.getHeight();
	}
	
	protected ArraySpace(final int width, final int height, final int totalPlanes) {
		planes = new ArrayPlane[totalPlanes];
		planeList = Collections.unmodifiableList(Arrays.asList(planes));
		for(int p = 0; p < totalPlanes; ++p) {
			planes[p] = new ArrayPlane(width, height, p);
		}
	}
	
}
