package com.podts.rpg.server.model.universe;

import java.util.Objects;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;

public abstract class Entity extends Spatial implements Registerable, MovableFacable {
	
	public static final Direction DEFAULT_FACE = Direction.UP;
	
	private static int nextID;
	
	private final int id;
	private String name;
	private final EntityType type;
	private Direction face;
	
	public final int getID() {
		return id;
	}
	
	public final EntityType getType() {
		return type;
	}
	
	public final String getName() {
		return name;
	}
	
	@Override
	public Direction getFacingDirection() {
		return face;
	}
	
	@Override
	public Entity face(final Direction dir) {
		Objects.requireNonNull(dir, "Cannot make Entity face null Direction!");
		face = dir;
		return this;
	}
	
	@Override
	public final Entity move(final int dx, final int dy, final int dz) {
		getSpace().moveEntity(this, MoveType.UPDATE, dx, dy, dz);
		return this;
	}
	
	@Override
	public final Entity move(final int dx, final int dy) {
		MovableFacable.super.move(dx, dy);
		return this;
	}
	
	@Override
	public final Entity move(final Direction dir) {
		MovableFacable.super.move(dir);
		return this;
	}
	
	public final boolean isRegistered() {
		return getSpace().isRegistered(this);
	}
	
	public final boolean register() {
		return getSpace().register(this);
	}
	
	public final void deRegister() {
		getSpace().deRegister(this);
	}
	
	public Entity(String name, EntityType type, Location loc, Direction face) {
		super(loc);
		id = nextID++;
		this.name = name;
		this.type = type;
		this.face = face;
	}
	
	public Entity(String name, EntityType type, Location loc) {
		this(name, type, loc, DEFAULT_FACE);
	}
	
	public Entity(EntityType type, Location loc, Direction face) {
		this(type.name(), type, loc, face);
	}
	
	public Entity(EntityType type, Location loc) {
		this(type, loc, DEFAULT_FACE);
	}
	
}
