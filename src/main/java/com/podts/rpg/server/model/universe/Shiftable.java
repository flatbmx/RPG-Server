package com.podts.rpg.server.model.universe;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.Direction;

public interface Shiftable<T extends Shiftable<T>> extends HasLocation {
	
	public T shift(int dx, int dy, int dz);
	
	public default T shift(int dx, int dy) {
		return shift(dx, dy, 0);
	}
	
	public default T shift(Vector vector) {
		return shift(vector.getX(), vector.getY(), vector.getZ());
	}
	
	public default T shift(Direction d, int distance) {
		return shift(d.getX(distance), d.getY(distance));
	}
	
	public default T shift(Direction d) {
		return shift(d.getX(), d.getY());
	}
	
	@SuppressWarnings("unchecked")
	public default Stream<? extends T> traceEvery(int dx, int dy, int dz) {
		if(dx == 0 && dy == 0 && dz == 0)
			return Stream.empty();
		return (Stream<? extends T>) Stream.iterate(this, s -> s.shift(dx, dy, dz));
	}
	
	@SuppressWarnings("unchecked")
	public default Stream<? extends T> traceEvery(int dx, int dy) {
		if(dx == 0 && dy == 0)
			return Stream.empty();
		return (Stream<? extends T>) Stream.iterate(this, s -> s.shift(dx, dy));
	}
	
	public default Stream<? extends T> traceEvery(Vector v) {
		return traceEvery(v.getX(), v.getY(), v.getZ());
	}
	
	public default Stream<? extends T> traceEvery(Direction dir, int increment) {
		if(dir == null)
			return Stream.empty();
		return traceEvery(dir.getX(increment), dir.getY(increment));
	}
	
	@SuppressWarnings("unchecked")
	public default Stream<? extends T> trace(final ShiftBehavior behavior) {
		if(behavior == null || !behavior.hasNext())
			return Stream.empty();
		return (Stream<? extends T>) Stream.iterate(this, s -> s.shift(behavior.next()))
				.takeWhile(s -> behavior.hasNext());
	}
	
	/**
	 * Returns an infinite Stream consisting of this Shiftable and all Shiftables in the given direction from closest to farthest in order.
	 * @param dir - The direction to shift this Shiftable.
	 * @return infinite Stream consisting of this Shiftable and all Shiftables in the given direction from closest to farthest in order
	 */
	public default Stream<? extends T> trace(Direction dir) {
		return traceEvery(dir, 1);
	}
	
	public default Stream<? extends T> trace(Direction dir, int distance) {
		distance = Math.max(0, distance);
		return trace(dir)
				.limit(distance + 1);
	}
	
	public default Stream<? extends T> traceTo(HasLocation loc) {
		if(isInDifferentSpace(loc))
			return Stream.empty();
		
		Optional<Direction> dirOpt = Direction.get(this, loc);
		if(!dirOpt.isPresent())
			return Stream.empty();
		
		return trace(dirOpt.get())
				.limit(walkingDistance(loc) + 1);
	}
	
	public default Stream<? extends T> bitraceEvery(int dx, int dy, int dz) {
		if(dx == 0 && dy == 0 && dz == 0)
			return Stream.empty();
		return IntStream.iterate(0, i -> {
			i *= -1;
			if(i >= 0)
				++i;
			return i;
		}).mapToObj(i -> shift(i*dx, i*dy, i*dz));
	}
	
	public default Stream<? extends T> bitraceEvery(int dx, int dy) {
		return bitraceEvery(dx, dy, 0);
	}
	
	public default Stream<? extends T> bitraceEvery(Vector v) {
		return bitraceEvery(v.getX(), v.getY(), v.getZ());
	}
	
	public default Stream<? extends T> bitraceEvery(Direction dir, int increment) {
		return bitraceEvery(dir.getX(increment), dir.getY(increment));
	}
	
	public default Stream<? extends T> bitrace(Direction dir) {
		return bitraceEvery(dir, 1);
	}
	
	public default Stream<? extends T> bitrace(Direction dir, int distance) {
		distance = Math.max(0, distance);
		return bitrace(dir)
				.limit(distance + 1);
	}
	
}
