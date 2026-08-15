package engine.gal.aut;

// == STATE ==

/**
 * An FSM state: a {@code mode} (the entity's behaviour label — e.g. "Walking",
 * "Fighting", "frightened" — which also selects the
 * {@link engine.graphics.Avatar} to show) plus an integer {@code id} so one
 * mode can have several numbered states ("Walking_1", "Walking_2", …). Per the
 * GAL spec a bot's current {@link State} drives both its automaton and its
 * appearance.
 *
 * <p>
 * Immutable, with value equality on {@code (mode, id)} so states can key maps
 * and be compared by content.
 */
public final class State {

	/**
	 * The GAL destroy state {@code ()}: reaching it means the entity must be
	 * destroyed. A unique sentinel (negative id, reserved mode) compared by
	 * identity in {@link Automaton#step}.
	 */
	public static final State DESTROY = new State("()", -1);

	/**
	 * The GAL non-deterministic target {@code (_)}: reaching it means jump to a
	 * random state of the automaton. A unique sentinel resolved in
	 * {@link Automaton#step}.
	 */
	public static final State RANDOM = new State("(_)", -2);

	final String mode;
	private final int id;

	public State(String mode, int id) {
		this.mode = mode;
		this.id = id;
	}

	public State(String mode) {
		this(mode, 0);
	}

	public String mode() {
		return mode;
	}

	public int id() {
		return id;
	}

	public boolean equals(State s) {
		return s != null && id == s.id && mode.equals(s.mode);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof State s && equals(s);
	}

	@Override
	public int hashCode() {
		return mode.hashCode() * 31 + id;
	}

	@Override
	public String toString() {
		return mode + "_" + id;
	}
}
