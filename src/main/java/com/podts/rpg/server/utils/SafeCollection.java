package com.podts.rpg.server.utils;

import java.util.Collection;

public interface SafeCollection<E> extends Collection<E> {
	
	public Collection<E> getSafe();
	
}
