package com.podts.rpg.server.model;

public interface Door {
	
	public boolean isOpen();
	
	public default boolean isClosed() {
		return !isOpen();
	}
	
	public Door setOpen(boolean open);
	
	public default Door open() {
		if(isClosed()) setOpen(true);
		return this;
	}
	
	public default Door close() {
		if(isOpen()) setOpen(false);
		return this;
	}
	
}
