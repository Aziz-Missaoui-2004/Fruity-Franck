package engine.gal.aut;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import engine.core.Model;
import engine.entities.Bot;
import engine.entities.Entity;

// == AUTOMATON ==

/**
 * A GAL finite-state machine: an initial {@link State} and an ordered list of
 * {@link Transition}s. {@link #step(Entity)} fires the first satisfied
 * transition out of the entity's current state (the order is the source order,
 * so earlier transitions have priority), or does nothing and stays put.
 *
 * <p>
 * The two GAL special targets are resolved here: reaching {@link State#DESTROY}
 * ({@code ()}) removes the entity from its model; reaching {@link State#RANDOM}
 * ({@code (_)}) jumps to a uniformly random state of this automaton.
 */
public class Automaton {

	private final String name;
	private final State initial;
	private final List<Transition> transitions = new ArrayList<>();
	/** Distinct concrete (non-sentinel) states, for {@code (_)} resolution. */
	private final Set<State> states = new LinkedHashSet<>();

	public Automaton(String name, State initial) {
		this.name = name;
		this.initial = initial;
		register(initial);
	}

	public String name() {
		return name;
	}

	public State initial() {
		return initial;
	}

	public void add(Transition t) {
		transitions.add(t);
		register(t.src());
		register(t.tgt());
	}

	/** Track a concrete state for random-target resolution (sentinels excluded). */
	private void register(State s) {
		if (s != null && s != State.DESTROY && s != State.RANDOM)
			states.add(s);
	}

	/** @return {@code true} if some transition fired this step. */
	public boolean step(Entity e) {
		for (Transition t : transitions) {
			if (!t.exec(e))
				continue;
			Bot bot = e.bot();
			State reached = bot.state();
			if (reached == State.DESTROY) {
				Model m = e.model();
				if (m != null)
					m.remove(e);
				else
					e.destroy();
			} else if (reached == State.RANDOM) {
				bot.state(randomState());
			}
			return true;
		}
		return false;
	}

	/**
	 * @return a uniformly random concrete state (or the initial if none collected).
	 */
	private State randomState() {
		if (states.isEmpty())
			return initial;
		int k = ThreadLocalRandom.current().nextInt(states.size());
		int i = 0;
		for (State s : states) {
			if (i++ == k)
				return s;
		}
		return initial;
	}
}
