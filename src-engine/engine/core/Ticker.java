package engine.core;

import engine.debug.Debug;
import oop.tasks.Task;
import oop.tasks.Runtime;
import oop.tasks.Runnable;

/**
 * Drives the game tick loop using the {@link Task} framework.
 *
 */
public class Ticker {

	/**
	 * Implemented by any class that needs to be notified of game ticks
	 */
	public interface TickListener {

		/**
		 * Called once per game tick.
		 *
		 * @param deltaTime the fixed duration of one tick, in seconds (e.g. 1.0/60 for
		 *                  60 TPS). Always the same value for a given Ticker
		 */
		void onTick(double deltaTime);
	}

	private final int targetTps;
	private final long tickDurationNanos; // fixed tick size in nanoseconds
	private final double tickDurationSeconds; // same value, passed to the listener as delta

	private Task tickerTask;
	private boolean running;

	/** Optional debug config; {@code null} = no instrumentation (the default). */
	private Debug debug;

	/**
	 * Attach a {@link Debug} so this ticker records inter-tick intervals when
	 * {@link Debug#showTickStats()} is on. Pass {@code null} to detach.
	 */
	public void setDebug(Debug debug) {
		this.debug = debug;
	}

	/**
	 * @param targetTps desired ticks per second (e.g. 20, 60, 120)
	 */
	public Ticker(int targetTps) {
		if (targetTps <= 0)
			throw new IllegalArgumentException("targetTps must be positive");
		this.targetTps = targetTps;
		this.tickDurationNanos = 1_000_000_000L / targetTps;
		this.tickDurationSeconds = 1.0 / targetTps;
	}

	/**
	 * Creates the ticker {@link Task} and posts the first tick. Does nothing if
	 * already running.
	 *
	 * @param listener the object to notify each tick (typically the game Model)
	 */
	public void start(TickListener listener) {
		if (running)
			return;
		running = true;
		tickerTask = Runtime.newTask("ticker");
		tickerTask.post(new TickRunnable(listener));
	}

	/**
	 * Kills the ticker task immediately. Any tick that is already executing will
	 * finish, but no further ticks will be scheduled.
	 */
	public void stop() {
		running = false;
		if (tickerTask != null)
			tickerTask.kill();
	}

	/** @return true if the ticker task is alive and ticking */
	public boolean isRunning() {
		return running && tickerTask != null && !tickerTask.dead();
	}

	/** @return the target ticks-per-second this Ticker was configured with */
	public int getTargetTps() {
		return targetTps;
	}

	// -------------------------------------------------------------------------

	/**
	 * The self-rescheduling runnable that drives the tick loop.
	 *
	 * Each execution: 1. notifies the listener with the fixed delta time 2.
	 * advances the internal clock by exactly one tick duration 3. computes the
	 * remaining delay until the next tick 4. posts itself again on the ticker task
	 * with that delay
	 *
	 */
	private class TickRunnable implements Runnable {

		private final TickListener listener;
		private long lastTickTime = -1; // nanoseconds; -1 means "not yet started"

		TickRunnable(TickListener listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			if (!running)
				return;

			long now = System.nanoTime();

			// Anchor the clock on the very first tick.
			if (lastTickTime < 0)
				lastTickTime = now;

			// Debug: record the interval since the previous tick (opt-in, off by default).
			if (debug != null && debug.showTickStats())
				debug.tickStats().markAndSample();

			// Notify the game model.
			listener.onTick(tickDurationSeconds);

			// Advance by exactly one fixed tick to avoid cumulative drift.
			lastTickTime += tickDurationNanos;

			// How many nanoseconds until the next tick should fire?
			long nextTickIn = lastTickTime - System.nanoTime();
			int delayMs = (int) Math.max(0L, nextTickIn / 1_000_000L);

			// Reschedule on the ticker task (which is Runtime.task() right now).
			Runtime.post(this, delayMs);
		}
	}
}