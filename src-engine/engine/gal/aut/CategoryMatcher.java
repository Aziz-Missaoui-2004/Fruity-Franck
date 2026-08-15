package engine.gal.aut;

import engine.entities.Entity;

// == CATEGORY MATCHER ==

/**
 * The game-provided binding between abstract GAL {@link Category categories}
 * (the letters {@code A}, {@code T}, …) and concrete game entities. The
 * interpreter is game-agnostic; it asks the matcher "does {@code other} count
 * as {@code cat} from {@code self}'s point of view?" — e.g. from a ghost's view
 * the player is an adversary ({@code A}).
 */
public interface CategoryMatcher {
	boolean matches(Category cat, Entity self, Entity other);

	/**
	 * The viewpoint-free default: {@code other} matches {@code cat} iff it is
	 * {@link Categorized} and declares that very category. Games use this directly
	 * for non-relational categories and wrap/override it only when a letter's
	 * meaning depends on {@code self} (teams, an adversary that is edible while
	 * frightened, …).
	 */
	CategoryMatcher BY_CATEGORY = (cat, self, other) -> other instanceof Categorized c && c.category() == cat;
}
