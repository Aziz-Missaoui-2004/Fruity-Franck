package game.ai;

import engine.entities.Entity;
import engine.gal.aut.Category;
import engine.gal.aut.CategoryMatcher;
import game.Categorized;

// == PACMAN CATEGORIES ==

/**
 * Binds the abstract GAL categories used by the Pac-Man automata to concrete
 * game entities. From a ghost's point of view the adversary ({@code A}) is the
 * player Pac. Non-game props match nothing (guarded by {@link Categorized}).
 */
public class PacManCategories implements CategoryMatcher {

	@Override
	public boolean matches(Category cat, Entity self, Entity other) {
		if (!(other instanceof Categorized))
			return false;
		game.Category oc = game.Category.of(other);
		if (cat == Category.Adversary)
			return oc == game.Category.PAC;
		return false;
	}
}
