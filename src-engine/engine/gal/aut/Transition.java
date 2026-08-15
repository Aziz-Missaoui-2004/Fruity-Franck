package engine.gal.aut;

import engine.entities.Bot;
import engine.entities.Entity;

// == TRANSITION ==

/**
 * One GAL transition {@code (src): condition ? action :(tgt)}. It fires only
 * when the bot is in {@code src}, {@code condition} holds, and {@code action}
 * can occur; firing runs {@code action} and switches the bot to {@code tgt}. If
 * the action cannot occur the transition is treated as not having fired.
 */
public class Transition {

	private final State src;
	private final iGALCondition condition;
	private final iGALAction action;
	private final State tgt;

	public Transition(State src, iGALCondition condition, iGALAction action, State tgt) {
		this.src = src;
		this.condition = condition;
		this.action = action;
		this.tgt = tgt;
	}

	public State src() {
		return src;
	}

	/**
	 * Try to fire this transition for {@code e}.
	 *
	 * <p>
	 * A transition fires only when the bot is in {@code src}, its {@code condition}
	 * holds, and its {@code action} can occur; if the action cannot occur
	 * the transition is considered not to have fired
	 *
	 * @return {@code true} if it fired (bot was in {@code src}, the condition held,
	 *         and the action occurred); {@code false} otherwise.
	 */
	public boolean exec(Entity e) {
		Bot bot = e.bot();
		if (bot != null && src.equals(bot.state()) && condition.eval(e) && action.exec(e)) {
			bot.state(tgt);
			return true;
		}
		return false;
	}

	/**
	 * @return the target state, for the automaton to resolve.
	 */
	public State tgt() {
		return tgt;
	}
}
