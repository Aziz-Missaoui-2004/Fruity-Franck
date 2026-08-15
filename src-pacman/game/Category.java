package game;

import engine.entities.Entity;
import game.Categorized;
import game.Category;

// == CATEGORY ==

/**
 * The game's own classification of its entities — the single discriminator
 */
public enum Category {
	PAC, GHOST, GUM, BOSS;

	/**
	 * The category of {@code e}. Every game entity implements {@link Categorized};
	 * this is the one place the engine&rarr;game boundary cast is performed.
	 */
	public static Category of(Entity e) {
		if (e instanceof Categorized c)
			return c.category();
		throw new IllegalArgumentException("not a categorized game entity: " + e);
	}
}
