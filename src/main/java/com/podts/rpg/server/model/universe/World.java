package com.podts.rpg.server.model.universe;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.Utils;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.TileElement.TileType;
import com.podts.rpg.server.model.universe.region.DynamicRegion;
import com.podts.rpg.server.model.universe.region.DynamicRegionListener;
import com.podts.rpg.server.model.universe.region.MonitoringRegion;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.model.universe.region.RegionListener;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.TilePacket;

/**
 * A collection of Tiles that represent a world that entities such as NPCs, players, etc can inhabit and interact with
 * each other and the world it self.
 *
 */
public abstract class World extends Space {
	
	private final WorldGenerator generator;
	private String name;
	
	protected static final Collection<Entity> EMPTY_ENTITIES = Collections.unmodifiableList(Collections.emptyList());
	
	/**
	 * Returns the name of the World.
	 * @return The Worlds name.
	 */
	public final String getName() {
		return name;
	}
	
	protected final void setName(String newName) {
		name = newName;
	}
	
	/**
	 * Returns the Worlds {@link WorldGenerator}.
	 * @return
	 */
	public final WorldGenerator getWorldGenerator() {
		return generator;
	}
	
	public abstract Collection<Player> getPlayers();
	
	@Override
	public Stream<Player> players() {
		return getPlayers().stream();
	}
	
	/**
	 * Returns the tile that is located at the given point in this World.
	 * @param point - The location of the Tile.
	 * @return The Tile that is located at the given point.
	 */
	public final Tile getTile(Location point) {
		Utils.assertNullArg(point, "Cannot get Tile for null location.");
		Utils.assertArg(!doContains(point), "Cannot get Tile that exists in a different World.");
		return doGetTile(point);
	}
	
	protected abstract Tile doGetTile(Location point);
	
	public final World getTiles(Tile[][] tiles, Location topLeft) {
		Utils.assertNullArg(tiles, "Cannot get Tiles with null array.");
		Utils.assertNullArg(topLeft, "Cannot get Tiles with null starting point.");
		Utils.assertArg(tiles.length == 0 || tiles[0].length == 0, "Cannot get Tiles with array length of 0.");
		Utils.assertArg(!doContains(topLeft), "Cannot get Tiles with starting point from a different world.");
		
		doGetTiles(tiles, topLeft);
		return this;
	}
	
	protected abstract void doGetTiles(Tile[][] tiles, Location topLeft);
	
	public final Collection<Entity> getNearbyEntities(HasLocation l, double distance) {
		return nearbyEntities(l, distance)
				.collect(Collectors.toSet());
	}
	
	/**
	 * This method is equivalent to calling {@link #getNearbyEntities(Locatable,Predicate) getNearbyEntities} with no condition.
	 * @param l - The central point of all entities.
	 * @return A Collection of entities within the given radius in this World.
	 */
	public final Collection<Entity> getNearbyEntities(HasLocation l) {
		return nearbyEntities(l)
				.collect(Collectors.toSet());
	}
	
	/**
	 * Returns the {@link #register(Entity) registered} {@link Entity} with the given id.
	 * @param id - The ID of the desired {@link Entity}.
	 * @return The {@link Entity} that has the given ID otherwise null if the {@link Entity} either does not exist or is not {@link #register(Entity) registered}.
	 */
	public abstract Entity getEntity(int id);
	
	public final Stream<Entity> entities(Stream<Integer> ids) {
		return ids.map(this::getEntity)
				.filter(Objects::nonNull);
	}
	
	public abstract Stream<Tile> nearbyTiles(HasLocation l);
	
	/**
	 * Registers an entity with this {@link World}.
	 * Any non-registered entities will not be viewable by {@link #getEntity(int)},
	 * {@link #getNearbyEntities(Locatable)} nor {@link #getNearbyEntities(Locatable,double)}.
	 * @param e - The {@link Entity} that is to be registered.
	 * @return True if the {@link Entity} was successfully registered, false if there already exists an 
	 * {@link Entity} that has the same ID in this {@link Word}.
	 */
	public final boolean register(Entity e) {
		boolean result = doRegister(e);
		if(result) {
			if(e instanceof PlayerEntity)
				sendToNearbyPlayers(e, ((PlayerEntity)e).getPlayer(), EntityPacket.constructCreate(e));
			else
				sendToNearbyPlayers(e, EntityPacket.constructCreate(e));
		}
			
		return result;
	}
	
	protected abstract boolean doRegister(Entity e);
	
	/**
	 * Un-registers an {@link Entity} that is already {@link #register(Entity) registered} in this {@link Word}.
	 * If the Entity is null or is not registered in this world it will silently return.
	 * @param e - The {@link Entity} that will be un-registered.
	 * @return The World for chaining.
	 */
	public final World deRegister(Entity e) {
		if(doDeRegister(e)) {
			sendToNearbyPlayers(e, EntityPacket.constructDestroy(e));
		}
		return this;
	}
	
	protected abstract boolean doDeRegister(Entity e);
	
	/**
	 * Registers the given {@link PollableRegion region} with this {@link World world}.
	 * {@link Region Regions} that are not registered will not have their
	 * {@link RegionListener#onEntityEnter(Region, Entity, MoveType) onEnter} nor {@link RegionListener#onEntityLeave(Region, Entity, MoveType) onLeave} methods called.
	 * If the {@link Region region} is already registered then it will follow any changes made to the Region.
	 * @param region - The given {@link PollableRegion region} to register.
	 * @return The {@link World world} for chaining.
	 */
	public final World register(PollableRegion region) {
		Utils.assertNull(region, "Cannot register a null region!");
		
		doRegister(region);
		if(region instanceof MonitoringRegion) {
			MonitoringRegion mR = (MonitoringRegion) region;
			mR.addEntities(findEntitiesIn(region));
		}
		if(region instanceof DynamicRegion) {
			region.addRegionListeners(regionChangeHandler);
		}
		return this;
	}
	
	protected abstract void doRegister(PollableRegion region);
	
	/**
	 * Returns true if the region is registered to this World.
	 * @param region - The region in question.
	 * @return true if the region is registered to this world, false if it is not registered.
	 */
	public abstract boolean isRegistered(PollableRegion region);
	
	/**
	 * Un-registers the given {@link PollableRegion region} from this World.
	 * If the {@link PollableRegion region} was not previously {@link #register(PollableRegion) registered} then nothing will happen.
	 * @param r - The given Region to un-register.
	 * @return The World for chaining.
	 */
	public final World unRegister(PollableRegion region) {
		if(region instanceof DynamicRegion) {
			region.removeRegionListeners(regionChangeHandler);
		}
		return this;
	}
	
	protected abstract void doUnRegister(PollableRegion r);
	
	/**
	 * Returns all registered {@link PollableRegion}s that contain a given {@link Location}.
	 * @param loc - The given point.
	 * @return A {@link Collection} of all the registered {@link PollableRegion}s that {@link Region#contains(Locatable) contains} the given point.
	 * The Collection may be modifiable or not however any changes will not affect anything outside of the Collection returned.
	 */
	public Collection<PollableRegion> getRegions(HasLocation loc) {
		return regions(loc)
				.collect(Collectors.toSet());
	}
	
	protected final Collection<Entity> findEntitiesIn(final PollableRegion region) {
		return entities(region)
				.collect(Collectors.toSet());
	}
	
	public final Stream<Entity> entities(final PollableRegion region) {
		Objects.requireNonNull(region, "Cannot get stream of entities from a null region!");
		if(region instanceof MonitoringRegion)
			return ((MonitoringRegion)region).entities();
		return doEntitiesIn(region);
	}
	
	abstract Stream<Entity> doEntitiesIn(final PollableRegion region);
	
	
	protected final World moveEntity(final Entity entity, final Location newLoc, final MoveType type) {
		
		if(getTile(newLoc).isVoid())
			return this;
		
		final Collection<Region>[] regionChanges = findRegionChanges(entity.getLocation(), newLoc);
		
		for(final Region r : regionChanges[0]) {
			//Old regions
			fireRegionLeave(r, entity, newLoc, type);
		}
		for(final Region r : regionChanges[1]) {
			//Stale regions
			fireRegionMove(r, entity, newLoc, type);
		}
		for(final Region r : regionChanges[2]) {
			//New regions
			fireRegionEnter(r, entity, newLoc, type);
		}
		
		doMoveEntity(entity, newLoc, type);
		
		//Update entity position to all viewers.
		sendToNearbyPlayers(entity, EntityPacket.constructUpdate(entity));
		
		return this;
	}
	
	private final Collection<Region>[] findRegionChanges(final Location start, final Location end) {
		final Set<Region> oldRegions = new HashSet<>(getRegions(start));
		final Set<Region> staleRegions = new HashSet<>();
		final Set<Region> newRegions = new HashSet<>(getRegions(end));
		
		final Iterator<Region> it = newRegions.iterator();
		while(it.hasNext()) {
			final Region r = it.next();
			if(oldRegions.contains(r)) {
				staleRegions.add(r);
				oldRegions.remove(r);
				it.remove();
			}
		}
		
		@SuppressWarnings("unchecked")
		final Collection<Region>[] result = (Collection<Region>[]) Array.newInstance(Collection.class, 3);
		
		result[0] = Collections.unmodifiableCollection(oldRegions);
		result[1] = Collections.unmodifiableCollection(staleRegions);
		result[2] = Collections.unmodifiableCollection(newRegions);
		return result;
	}
	
	protected abstract World doMoveEntity(Entity entity, Location newLocation, MoveType type);
	
	final Location moveEntity(Entity entity, MoveType type, int dx, int dy, int dz) {
		Location result = entity.getLocation().shift(dx, dy, dz);
		moveEntity(entity, result, type);
		return result;
	}
	
	protected final Location moveEntity(Entity entity, MoveType type, int dx, int dy) {
		return moveEntity(entity, type, dx, dy, 0);
	}
	
	@Override
	public Location createLocation(int x, int y, int z) {
		return new CompleteLocation(this, x, y, z);
	}
	
	public final Tile createTile(final TileType type, final int x, final int y, final int z) {
		Utils.assertNullArg(type, "Cannot create Tile with null type!");
		return new Tile(type, createLocation(x,y,z));
	}
	
	public final boolean contains(final HasLocation loc) {
		Utils.assertNullArg(loc, "Cannot determine if null Locatable is in World.");
		return doContains(loc);
	}
	
	protected final boolean doContains(final HasLocation loc) {
		return equals(loc.getSpace());
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	private static final void fireRegionEnter(Region r, Entity entity, Location newLocation, MoveType type) {
		if(r instanceof MonitoringRegion) {
			((MonitoringRegion) r).addEntity(entity);
		}
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityEnter(r, entity, type);
		}
	}
	
	private static final void fireRegionMove(Region r, Entity entity, Location newLocation, MoveType type) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityMove(r, entity, type);
		}
	}
	
	private static final void fireRegionLeave(Region r, Entity entity, Location newLocation, MoveType type) {
		if(r instanceof MonitoringRegion) {
			((MonitoringRegion) r).removeEntity(entity);
		}
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityLeave(r, entity, type);
		}
	}
	
	private final class RegionChangeListener implements DynamicRegionListener {
		@Override
		public void onRegionChange(Region r) {
			handleRegionChange((PollableRegion) r);
		}
	}
	
	private final RegionListener regionChangeHandler = new RegionChangeListener();
	
	private final void followRegion(DynamicRegion r) {
		r.addRegionListeners(regionChangeHandler);
	}
	
	private final void unFollowRegion(DynamicRegion r) {
		r.removeRegionListeners(regionChangeHandler);
	}
	
	protected abstract void handleRegionChange(PollableRegion r);
	
	protected final void sendCreateEntity(Entity entity, Player... players) {
		final Packet packet = EntityPacket.constructCreate(entity);
		for(final Player player : players)
			player.sendPacket(packet);
	}
	
	protected final void sendUpdateEntity(Player player, Entity entity, Location newLocation) {
		player.sendPacket(EntityPacket.constructMove(entity, newLocation));
	}
	
	protected final void sendDestroyEntity(Entity entity, Player... players) {
		final Packet packet = EntityPacket.constructDestroy(entity);
		for(final Player player : players)
			player.sendPacket(packet);
	}
	
	protected World(final String name, final WorldGenerator generator) {
		this.name = name;
		this.generator = generator;
	}
	
}
