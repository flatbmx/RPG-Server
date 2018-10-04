package com.podts.rpg.server.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public final class SafeCollection<E> implements Collection<E> {
	
	private final Collection<E> collection, safeCollection;
	
	public final Collection<E> getSafe() {
		return safeCollection;
	}
	
	@Override
	public int size() {
		return collection.size();
	}
	
	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return collection.contains(o);
	}
	
	@Override
	public Iterator<E> iterator() {
		return collection.iterator();
	}
	
	@Override
	public Object[] toArray() {
		return collection.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return collection.toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		return collection.add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		return collection.remove(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return collection.containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		return collection.addAll(c);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return collection.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return collection.retainAll(c);
	}
	
	@Override
	public void clear() {
		collection.clear();
	}
	
	public SafeCollection(Collection<E> collection) {
		this.collection = collection;
		safeCollection = Collections.unmodifiableCollection(collection);
	}
	
}
