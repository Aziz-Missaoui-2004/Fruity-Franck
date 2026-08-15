package game.ai;

import static engine.gal.aut.Gal.automaton;
import static engine.gal.aut.Gal.closest;
import static engine.gal.aut.Gal.move;
import static engine.gal.aut.Gal.on;
import static engine.gal.aut.Gal.opp;
import static engine.gal.aut.Gal.selectedDir;
import static engine.gal.aut.Gal.state;

import engine.gal.aut.Automaton;
import engine.gal.aut.Category;
import engine.gal.aut.State;

// == PACMAN AUTOMATA ==

/**
 * The Pac-Man ghost behaviours, hand-built from the provided {@code .gal} files
 * until the teachers' parser lands.
 */
public final class PacManAutomata {

	private PacManAutomata() {
	}

	/**
	 * Chase the player. Mirrors {@code gal_fsm/Tracking.gal}:
	 *
	 * <pre>
	 * * (Tracking): | Closest(A) ? Move($.d) :(Tracking)
	 * </pre>
	 */
	public static Automaton tracking() {
		State s = state("Tracking");
		return automaton("Tracking", s, on(s, closest(Category.Adversary), move(selectedDir()), s));
	}

	/**
	 * Flee the player. Mirrors {@code gal_fsm/Flee.gal}:
	 *
	 * <pre>
	 * * (Flee): | Closest(A) ? Move(Opp($.d)) :(Flee)
	 * </pre>
	 */
	public static Automaton flee() {
		State s = state("Flee");
		return automaton("Flee", s, on(s, closest(Category.Adversary), move(opp(selectedDir())), s));
	}
}
