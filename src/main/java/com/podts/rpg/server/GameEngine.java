package com.podts.rpg.server;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class GameEngine {
	
	private static GameEngine instance;
	
	public static final GameEngine get() {
		return instance;
	}
	
	protected static void create(int poolSize) {
		instance = new GameEngine(poolSize);
	}
	
	private final ScheduledThreadPoolExecutor executor;
	
	public <T> Future<T> submit(Callable<T> task) {
		return executor.submit(task);
	}
	
	public Future<?> submit(Runnable task) {
		return executor.submit(task);
	}
	
	public <T> Future<T> submit(Runnable task, T result) {
		return submit(task, result);
	}
	
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
	}
	
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}
	
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return executor.schedule(callable, delay, unit);
	}
	
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return executor.schedule(command, delay, unit);
	}
	
	protected void shutdown() {
		executor.shutdown();
	}
	
	private GameEngine(int poolSize) {
		executor = new ScheduledThreadPoolExecutor(poolSize);
	}
	
}
