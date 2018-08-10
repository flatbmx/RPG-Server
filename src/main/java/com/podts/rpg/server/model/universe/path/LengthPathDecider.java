package com.podts.rpg.server.model.universe.path;

public class LengthPathDecider implements PathDecider {
	
	private final int maxLength;
	
	public final int getMaxLength() {
		return maxLength;
	}
	
	@Override
	public boolean test(Path path) {
		return path.length() <= maxLength;
	}
	
	public LengthPathDecider(int maxLength) {
		this.maxLength = maxLength;
	}
	
}
