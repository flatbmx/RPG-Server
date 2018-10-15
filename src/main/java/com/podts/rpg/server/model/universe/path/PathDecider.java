package com.podts.rpg.server.model.universe.path;

import java.util.Comparator;
import java.util.function.Predicate;

public interface PathDecider extends Predicate<Path>, Comparator<Path> {
	
	@Override
	public default int compare(Path a, Path b) {
		final int lengthDiff = a.length() - b.length();
		if(lengthDiff != 0)
			return lengthDiff;
		return a.getTurns() - b.getTurns();
	}
	
}
