package game;

import java.util.EnumMap;
import engine.entities.Entity;
import game.entities.Gum;
import game.entities.Pac;


// == RULES ==

/**
 * The Pac-Man collision matrix: what happens when an entity of one
 * {@link Category} meets another, as a function of the two categories and the
 * {@link GameState}. Stored as a table rather than an {@code instanceof} chain,
 * so it is order-independent and open to new rules without editing a dispatch
 * method — add a line in the constructor.
 */
public class Rules {

	/** How many motion ticks a power pellet keeps the ghosts frightened. */
	public static final int FRIGHTENED_TICKS = 300;

	/**
	 * The outcome of one ordered collision: "{@code self} acts on {@code other}".
	 */
	@FunctionalInterface
	public interface Outcome {
		void apply(PacManModel model, GameState state, Entity self, Entity other);
	}

	private final EnumMap<Category, EnumMap<Category, Outcome>> table = new EnumMap<>(Category.class);

	public Rules() {
		// Pac eats a gum: gum gone, score up. A power pellet also starts frightened
		// mode and powers Pac (so the PAC×GHOST rule below lets him eat the ghost).
		on(Category.PAC, Category.GUM, (m, s, pac, gum) -> {
			m.remove(gum);
			if (gum instanceof Gum g && g.isPower()) {
				s.addScore(50);
				s.frighten(FRIGHTENED_TICKS);
				((Pac) pac).setPowered(true);
			} else {
				s.addScore(10);
			}
		});
		// Pac meets a ghost: a powered Pac eats it, otherwise Pac dies.
		on(Category.PAC, Category.GHOST, (m, s, pac, ghost) -> {
			if (((Pac) pac).isPowered()) {
				m.remove(ghost);
				s.addScore(200);
			} else {
				s.killPac();
				m.remove(pac);
			}
		});
		// ghost-ghost, ghost-gum, boss-* … : no rule registered -> no effect.
	}

	/**
	 * Resolve a confirmed (unordered) collision: try the rule for {@code (a, b)},
	 * then for the swap {@code (b, a)}.
	 */
	public void resolve(PacManModel model, GameState state, Entity a, Entity b) {
		if (apply(model, state, a, b))
			return;
		apply(model, state, b, a);
	}

	private void on(Category self, Category other, Outcome outcome) {
		table.computeIfAbsent(self, k -> new EnumMap<>(Category.class)).put(other, outcome);
	}

	private boolean apply(PacManModel model, GameState state, Entity self, Entity other) {
		EnumMap<Category, Outcome> row = table.get(Category.of(self));
		if (row == null)
			return false;
		Outcome outcome = row.get(Category.of(other));
		if (outcome == null)
			return false;
		outcome.apply(model, state, self, other);
		return true;
	}
}
