package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.TileElement.TileType;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.model.universe.region.RegionListener;

public class Tile extends Spatial implements Shiftable<Tile>, Region, Registerable {
	
	public static final Tile validate(Tile tile) {
		if(tile == null)
			return Space.NOWHERE_TILE;
		return tile;
	}
	
	private Set<RegionListener> regionListeners, safeRegionListeners;
	private Set<TileListener> tileListeners, safeTileListeners;
	TileElement element;
	
	final Collection<TileListener> getTileListeners() {
		if(noTileListeners())
			return Collections.emptySet();
		return safeTileListeners;
	}
	
	final Stream<TileListener> tileListeners() {
		if(noTileListeners())
			return Stream.empty();
		return getTileListeners().stream();
	}
	
	final Iterator<TileListener> tileListenerIterator() {
		if(noTileListeners())
			return Collections.emptyIterator();
		return tileListeners.iterator();
	}
	
	public final Tile addTileListener(TileListener Listener) {
		Objects.requireNonNull(Listener, "Cannot add null TileListener to " + this);
		return doAddTileListener(Listener);
	}
	
	final Tile doAddTileListener(TileListener handler) {
		onAddTileListener();
		if(tileListeners.add(handler))
			handler.onAdd(this);
		return this;
	}
	
	public final Tile removeTileHandler(TileListener listener) {
		Objects.requireNonNull(listener, "Cannot remove a null TileListener from " + this);
		if(noTileListeners())
			return this;
		return doRemoveTileListener(listener);
	}
	
	final Tile doRemoveTileListener(TileListener listener) {
		if(tileListeners.remove(listener)) {
			listener.onRemove(this);
			onRemoveTileListener();
		}
		return this;
	}
	
	private final boolean noTileListeners() {
		return tileListeners == null;
	}
	
	private void onAddTileListener() {
		if(tileListeners == null) {
			tileListeners = new HashSet<>();
			safeTileListeners = Collections.unmodifiableSet(tileListeners);
		}
	}
	
	private void onRemoveTileListener() {
		if(tileListeners.isEmpty()) {
			tileListeners = null;
			safeTileListeners = null;
		}
	}
	
	@Override
	public Collection<? extends RegionListener> getRegionListeners() {
		if(noRegionListeners())
			return Collections.emptySet();
		return safeRegionListeners;
	}
	
	Iterator<RegionListener> regionListenerIterator() {
		return regionListeners.iterator();
	}
	
	@Override
	public Tile addRegionListeners(RegionListener... listeners) {
		Objects.requireNonNull(listeners, "Cannot add null RegionListeners[] to " + this);
		if(listeners.length == 0)
			return this;
		for(RegionListener l : listeners)
			Objects.requireNonNull(l, "Cannot add null RegionListener to " + this);
		return doAddRegionListeners(listeners);
	}
	
	Tile doAddRegionListeners(RegionListener... listeners) {
		onAddRegionListener();
		for(RegionListener l : listeners)
			regionListeners.add(l);
		return this;
	}
	
	@Override
	public Tile removeRegionListeners(RegionListener... listeners) {
		Objects.requireNonNull(listeners, "Cannot remove null RegionListeners[] from " + this);
		if(listeners.length == 0)
			return this;
		return doRemoveRegionListeners(listeners);
	}
	
	Tile doRemoveRegionListeners(RegionListener... listeners) {
		for(RegionListener l : listeners)
			regionListeners.remove(l);
		onRemoveRegionListener();
		return this;
	}
	
	private boolean noRegionListeners() {
		return regionListeners == null;
	}
	
	private void onAddRegionListener() {
		if(noRegionListeners()) {
			regionListeners = new HashSet<>();
			safeRegionListeners = Collections.unmodifiableSet(regionListeners);
		}
	}
	
	private void onRemoveRegionListener() {
		if(regionListeners.isEmpty()) {
			regionListeners = null;
			safeRegionListeners = null;
		}
	}
	
	@Override
	public final boolean contains(Location point) {
		return isAt(point);
	}
	
	public final TileElement getElement() {
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
	
	public final TileType getType() {
		return getElement().getType();
	}
	
	public final boolean is(TileType type) {
		return getType().equals(type);
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
	
	@Override
	public Tile shift(int dx, int dy, int dz) {
		return getLocation().shift(dx, dy, dz).getTile();
	}
	
	@Override
	public Tile shift(int dx, int dy) {
		return getLocation().shift(dx, dy).getTile();
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
	
	Tile(Location location) {
		super(location);
	}
	
}
