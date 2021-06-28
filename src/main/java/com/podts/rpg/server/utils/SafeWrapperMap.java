package com.podts.rpg.server.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

class SafeWrapperMap<K, V> implements SafeMap<K,V> {
	
	private final Map<K,V> map, safeMap;
	
	protected final Map<K,V> getRaw() {
		return map;
	}
	
	@Override
	public final Map<K,V> getSafe() {
		return safeMap;
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
	public boolean containsKey(Object key) {
		return getRaw().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return getRaw().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return getRaw().get(key);
	}

	@Override
	public V put(K key, V value) {
		return getRaw().put(key, value);
	}

	@Override
	public V remove(Object key) {
		return getRaw().remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		getRaw().putAll(m);
	}

	@Override
	public void clear() {
		getRaw().clear();
	}

	@Override
	public Set<K> keySet() {
		return getRaw().keySet();
	}

	@Override
	public Collection<V> values() {
		return getRaw().values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return getRaw().entrySet();
	}
	
	SafeWrapperMap(Map<K,V> map) {
		this.map = map;
		this.safeMap = Collections.unmodifiableMap(map);
	}
	
}
