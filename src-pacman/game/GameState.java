package game;

// == GAME STATE ==

/**
 * Mutable global state of a Pac-Man game: the flags and counters that are
 * <i>not</i> intrinsic to any single entity. Per-entity state (e.g. a Pac's
 * power-up, an entity's GAL inventory) stays on the entity; this holds what is
 * global — the score and whether Pac is alive today, and later lives, level, a
 * frightened-mode timer, and special events.
 *
 * <p>
 * Keeping it separate from {@link Rules} lets the rules read/write game state
 * as pure data and makes both unit-testable independently of the engine
 * cadence.
 */
public class GameState {

	private int score;
	private boolean pacAlive = true;
	private int frightenedTicks; // motion ticks remaining of frightened mode (0 = off)

	public int score() {
		return score;
	}

	public void addScore(int points) {
		score += points;
	}

	public boolean pacAlive() {
		return pacAlive;
	}

	public void killPac() {
		pacAlive = false;
	}

	// FRIGHTENED MODE (power pellet)

	/** Start (or refresh) frightened mode for {@code ticks} motion ticks. */
	public void frighten(int ticks) {
		frightenedTicks = ticks;
	}

	/** @return whether frightened mode is currently active. */
	public boolean frightened() {
		return frightenedTicks > 0;
	}

	/**
	 * Count down one motion tick of frightened mode; returns true while still
	 * active.
	 */
	public boolean tickFrightened() {
		if (frightenedTicks > 0)
			frightenedTicks--;
		return frightenedTicks > 0;
	}
}
