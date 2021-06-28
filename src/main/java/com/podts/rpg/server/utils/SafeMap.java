package com.podts.rpg.server.utils;

import java.util.Map;

public interface SafeMap<K,V> extends Map<K,V> {
	
	public Map<K,V> getSafe();
	
}
