package game;



// == CATEGORIZED ==

/**
 * A game entity that knows its own {@link Category}. Implemented by every
 * Pac-Man entity so that rendering, collision {@link Rules}, and the GAL
 * interpreter can dispatch on the game's own classification instead of Java
 * {@code instanceof}.
 */
public interface Categorized {
	Category category();
}
