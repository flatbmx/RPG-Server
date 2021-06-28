package com.podts.rpg.server.utils;

import java.util.Collection;
import java.util.Map;

public final class SafeCollections {
	
	public static <E> SafeCollection<E> wrap(final Collection<E> c) {
		return new SafeWrapperCollection<>(c);
	}
	
	public static <K,V> SafeMap<K,V> wrap(final Map<K,V> m) {
		return new SafeWrapperMap<K,V>(m);
	}
	
}
