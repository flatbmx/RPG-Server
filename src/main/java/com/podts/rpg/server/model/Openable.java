package com.podts.rpg.server.model;

public interface Openable {
	
	public boolean isOpen();
	
	public default boolean isClosed() {
		return !isOpen();
	}
	
	public Openable setOpen(boolean open);
	
	public default Openable open() {
		if(isClosed()) setOpen(true);
		return this;
	}
	
	public default Openable close() {
		if(isOpen()) setOpen(false);
		return this;
	}
	
}
