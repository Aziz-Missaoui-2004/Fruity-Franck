package engine.debug;

import java.util.ArrayDeque;
import java.util.Deque;

// == STATS ==

/**
 * Rolling-window min/max/average for a stream of samples.
 *
 * <p>
 * Samples older than a fixed time window (default 1&nbsp;s) are evicted, so the
 * reported figures reflect <i>recent</i> behaviour.
 */
public final class Stats {

	/** A timestamped sample, kept until it ages out of the window. */
	private static final class Sample {
		final long t; // nanoTime
		final double v;

		Sample(long t, double v) {
			this.t = t;
			this.v = v;
		}
	}

	private static final long DEFAULT_WINDOW_NS = 1_000_000_000L; // 1 s

	private final long windowNanos;
	private final Deque<Sample> samples = new ArrayDeque<>();

	/** Timestamp of the previous {@link #markAndSample()} call; -1 = none yet. */
	private long lastMarkNanos = -1;

	public Stats() {
		this(DEFAULT_WINDOW_NS);
	}

	public Stats(long windowNanos) {
		this.windowNanos = windowNanos;
	}

	/** Record one value (in the unit the caller reports, e.g. milliseconds). */
	public synchronized void sample(double value) {
		long now = System.nanoTime();
		samples.addLast(new Sample(now, value));
		evict(now);
	}

	/**
	 * Record the elapsed milliseconds since the previous call to this method.
	 * Returns the delta in ms, or {@code 0} on the very first call (no previous
	 * mark to measure against, so nothing is sampled).
	 */
	public synchronized double markAndSample() {
		long now = System.nanoTime();
		double deltaMs = 0;
		if (lastMarkNanos >= 0) {
			deltaMs = (now - lastMarkNanos) / 1_000_000.0;
			samples.addLast(new Sample(now, deltaMs));
			evict(now);
		}
		lastMarkNanos = now;
		return deltaMs;
	}

	/** Forget all samples and the last mark (e.g. on a debug toggle). */
	public synchronized void reset() {
		samples.clear();
		lastMarkNanos = -1;
	}

	// QUERIES (over the current window)

	public synchronized boolean isEmpty() {
		evict(System.nanoTime());
		return samples.isEmpty();
	}

	public synchronized double min() {
		evict(System.nanoTime());
		double m = Double.POSITIVE_INFINITY;
		for (Sample s : samples)
			m = Math.min(m, s.v);
		return samples.isEmpty() ? 0 : m;
	}

	public synchronized double max() {
		evict(System.nanoTime());
		double m = Double.NEGATIVE_INFINITY;
		for (Sample s : samples)
			m = Math.max(m, s.v);
		return samples.isEmpty() ? 0 : m;
	}

	public synchronized double avg() {
		evict(System.nanoTime());
		if (samples.isEmpty())
			return 0;
		double sum = 0;
		for (Sample s : samples)
			sum += s.v;
		return sum / samples.size();
	}

	// HELPERS

	/** Drop samples that have aged past the window relative to {@code now}. */
	private void evict(long now) {
		long cutoff = now - windowNanos;
		while (!samples.isEmpty() && samples.peekFirst().t < cutoff)
			samples.removeFirst();
	}
}
