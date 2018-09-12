package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.TileElement.TileType;

public class Tile extends Spatial implements Registerable {
	
	private Set<TileHandler> handlers, safeHandlers;
	TileElement element;
	
	final Collection<TileHandler> getHandlers() {
		if(noHandlers())
			return Collections.emptySet();
		return safeHandlers;
	}
	
	final Stream<TileHandler> handlers() {
		if(noHandlers())
			return Stream.empty();
		return getHandlers().stream();
	}
	
	final Iterator<TileHandler> handlerIterator() {
		if(noHandlers())
			return Collections.emptyIterator();
		return handlers.iterator();
	}
	
	public final Tile addHandler(TileHandler handler) {
		Objects.requireNonNull(handler, "Cannot add null TileHandler to " + this);
		return doAddHandler(handler);
	}
	
	final Tile doAddHandler(TileHandler handler) {
		onAdd();
		if(handlers.add(handler))
			handler.onAdd(this);
		return this;
	}
	
	public final Tile removeHandler(TileHandler handler) {
		Objects.requireNonNull(handler, "Cannot remove a null TileHandler from " + this);
		if(noHandlers())
			return this;
		return doRemoveHandler(handler);
	}
	
	final Tile doRemoveHandler(TileHandler handler) {
		if(handlers.remove(handler)) {
			handler.onRemove(this);
			onRemove();
		}
		return this;
	}
	
	private boolean noHandlers() {
		return handlers == null;
	}
	
	private void onAdd() {
		if(handlers == null) {
			handlers = new HashSet<>();
			safeHandlers = Collections.unmodifiableSet(handlers);
		}
	}
	
	private void onRemove() {
		if(handlers.isEmpty()) {
			handlers = null;
			safeHandlers = null;
		}
	}
	
	public TileElement getElement() {
		return element;
	}
	
	final Tile update() {
		getSpace().updateTile(this);
		return this;
	}
	
	@Override
	public final Tile getTile() {
		return this;
	}
	
	public final boolean isGenerated() {
		return getElement() != null;
	}
	
	public final TileType getType() {
		if(!isGenerated())
			return TileType.VOID;
		return getElement().getType();
	}
	
	public final boolean isVoid() {
		return TileType.VOID.equals(getType());
	}
	
	public final boolean isNotVoid() {
		return !isVoid();
	}
	
	public final boolean isTraversable() {
		return getSpace().doIsTraversable(this);
	}
	
	public Stream<Tile> traceTo(HasLocation loc) {
		if(isInDifferentSpace(loc))
			return Stream.empty();
		
		Direction dir = Direction.get(this, loc);
		if(dir == null)
			return Stream.empty();
		
		return trace(dir)
				.limit(walkingDistance(loc) + 1);
	}
	
	public Stream<Tile> traceEvery(Direction dir, int increment) {
		return Stream.iterate(this, tile -> tile.shift(dir, increment));
	}
	
	public Stream<Tile> trace(Direction dir) {
		return traceEvery(dir, 1);
	}
	
	public Stream<Tile> biTraceEvery(Direction dir, int increment) {
		return getLocation().bitraceEvery(dir, increment)
				.map(Location::getTile);
	}
	
	public Stream<Tile> biTrace(Direction dir) {
		return biTraceEvery(dir, 1);
	}
	
	public Tile shift(int dx, int dy, int dz) {
		return getLocation().shift(dx, dy, dz).getTile();
	}
	
	public Tile shift(int dx, int dy) {
		return getLocation().shift(dx, dy).getTile();
	}
	
	public Tile shift(Direction dir, int distance) {
		return shift(dir.getX(distance), dir.getY(distance));
	}
	
	public Tile shift(Direction dir) {
		return shift(dir, 1);
	}
	
	@Override
	public String toString() {
		return "[" + getType() + " " + getLocation() + "]";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getType(), getLocation());
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(o instanceof Tile) {
			Tile t = (Tile) o;
			return getElement().equals(t.getElement()) &&
					getLocation().equals(t.getLocation());
		}
		return false;
	}
	
	Tile(TileElement element, Location location) {
		super(location);
		this.element = element;
	}
	
	Tile(TileType type, Location location) {
		this(new TileElement(type), location);
	}
	
	Tile(TileElement element) {
		this(element, null);
	}
	
	Tile(TileType type) {
		this(new TileElement(type), null);
	}
	
	Tile(Location point) {
		super(point);
	}
	
}
