package com.podts.rpg.server.model.universe;

import java.util.Collection;

import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.model.universe.region.RegionListener;
import com.podts.rpg.server.model.universe.region.SimpleRegionHandler;

/**
 * A collection of Tiles that represent a world that entities such as NPCs, players, etc can inhabit and interact with
 * each other and the world it self.
 *
 */
public abstract class World extends SimpleRegionHandler implements Region {
	
	private final WorldGenerator generator;
	private String name;
	
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
	
	/**
	 * Returns the tile that is located at the given point in this World.
	 * @param point - The location of the Tile.
	 * @return The Tile that is located at the given point.
	 */
	public abstract Tile getTile(Locatable point);
	
	/**
	 * Set the the given Tile at the given point in this World.
	 * @param newTile - The new Tile.
	 * @param point - The location of the new Tile.
	 * @return The World for chaining.
	 */
	public abstract World setTile(Tile newTile, Location point);
	
	/**
	 * Returns all registered entities that are within a given distance around a given point in this World.
	 * All entities are in the same Z plane as the given point.
	 * The {@link Collection} may be modifiable or not however any modifications will <b>NOT</b> represent any World changes.
	 * @param l - The central point
	 * @param distance - The radius of entities around the given point.
	 * @return A Collection of entities within the given radius in this World.
	 */
	public abstract Collection<Entity> getNearbyEntities(Locatable l, double distance);
	
	/**
	 * Returns all nearby registered entities relative to a given point in this World.
	 * All entities are in the same Z plane as the given point.
	 * It is up to the Worlds decision as how "nearby" is defined.
	 * Different World implementations will have different ranges based on their native backings.
	 * The {@link Collection} may be modifiable or not however any modifications will <b>NOT</b> represent any World changes.
	 * @param l - The central point of all entities.
	 * @return A Collection of entities within the given radius in this World.
	 */
	public abstract Collection<Entity> getNearbyEntities(Locatable l);
	
	/**
	 * Returns the {@link #register(Entity) registered} {@link Entity} with the given id.
	 * @param id - The ID of the desired {@link Entity}.
	 * @return The {@link Entity} that has the given ID otherwise null if the {@link Entity} either does not exist or is not {@link #register(Entity) registered}.
	 */
	public abstract Entity getEntity(int id);
	
	/**
	 * Registers an entity with this {@link Word}.
	 * Any non-registered entities will not be viewable by {@link #getEntity(int)},
	 * {@link #getNearbyEntities(Locatable)} nor {@link #getNearbyEntities(Locatable,double)}.
	 * @param e - The {@link Entity} that is to be registered.
	 * @return True if the {@link Entity} was sucessfully registered, false if there already exists an 
	 * {@link Entity} that has the same ID in this {@link Word}.
	 */
	public abstract boolean register(Entity e);
	
	/**
	 * Un-registers an {@link Entity} that is already {@link #register(Entity) registered} in this {@link Word}.
	 * If the Entity is null or is not registered in this world it will silently return.
	 * @param e - The {@link Entity} that will be un-registered.
	 * @return The World for chaining.
	 */
	public abstract World deRegister(Entity e);
	
	/**
	 * Registers the given {@link PollableRegion} with this {@link World}.
	 * {@link Region}s that are not registered will not have their
	 * {@link RegionListener#onEnter(Region, Entity, MoveType)} nor {@link RegionListener#onLeave(Region, Entity, MoveType)} methods called.
	 * If the {@link Region} is already registered then it will follow any changes made to the Region.
	 * @param r - The given {@link PollableRegion} to register.
	 * @return The {@link World} for chaining.
	 */
	public abstract World registerRegion(PollableRegion r);
	
	/**
	 * Un-registers the given {@link PollableRegion region} from this World.
	 * If the {@link PollableRegion region} was not previously {@link #registerRegion(PollableRegion) registered} then nothing will happen.
	 * @param r - The given Region to un-register.
	 * @return The World for chaining.
	 */
	public abstract World deRegisterRegion(PollableRegion r);
	
	/**
	 * Returns all registered {@link PollableRegion}s that contain a given {@link Location}.
	 * @param loc - The given point.
	 * @return A {@link Collection} of all the registered {@link PollableRegion}s that {@link Region#contains(Locatable) contains} the given point.
	 * The Collection may be modifiable or not however any changes will not affect anything outside of the Collection returned.
	 */
	public abstract Collection<Region> getRegionsAtLocation(Locatable loc);
	
	/**
	 * Returns all {@link Entity}s that are within given {@link PollableRegion}.
	 * The Region does <b>NOT</b> have to be registered for this method to operate.
	 * @param r - The given {@link PollableRegion} that all entities are in.
	 * @return A {@link Collection} that contains all entities within this {@link Region}. The Collection may be modifable or not
	 * however any modifications will not affect the Entities, Region or World in any way.
	 */
	public abstract Collection<Entity> getEntitiesInRegion(PollableRegion r);
	
	protected abstract World moveEntity(Entity entity, Location newLocation);
	
	protected abstract Location moveEntity(Entity entity, int dx, int dy, int dz);
	
	public abstract Location createLocation(int x, int y, int z);
	
	@Override
	public final boolean contains(Locatable loc) {
		if(loc == null) throw new IllegalArgumentException("Cannot determine if null Locatable is in World.");
		return equals(loc.getWorld());
	}
	
	@Override
	public String toString() {
		return "World - " + name;
	}
	
	protected World(String name, WorldGenerator generator) {
		this.name = name;
		this.generator = generator;
	}
	
}
