package com.podts.rpg.server.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

class SafeWrapperCollection<E> implements SafeCollection<E> {
	
	private final Collection<E> coll, safeColl;
	
	protected final Collection<E> getRaw() {
		return coll;
	}
	
	@Override
	public final Collection<E> getSafe() {
		return safeColl;
	}
	
	@Override
	public int size() {
		return getRaw().size();
	}
	
	@Override
	public boolean isEmpty() {
		return getRaw().isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return getRaw().contains(o);
	}
	
	@Override
	public Iterator<E> iterator() {
		return getSafe().iterator();
	}
	
	@Override
	public Object[] toArray() {
		return getSafe().toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return getSafe().toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		return getRaw().add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		return getRaw().remove(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return getSafe().containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		return getRaw().addAll(c);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return getRaw().removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return getRaw().retainAll(c);
	}
	
	@Override
	public void clear() {
		getRaw().clear();
	}
	
	SafeWrapperCollection(Collection<E> c) {
		this.coll = c;
		this.safeColl = Collections.unmodifiableCollection(c);
	}
	
}
