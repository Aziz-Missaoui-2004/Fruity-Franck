package engine.gal.aut;

// == CATEGORIZED ==

/**
 * An entity that declares its own GAL {@link Category} — the single
 * classification the engine and the GAL interpreter dispatch on. A game's
 * entities implement this so the default {@link CategoryMatcher} can recognise
 * them without any game-specific wiring; a game only supplies a custom matcher
 * when matching is relational (depends on the viewpoint entity — teams, "edible
 * while frightened", …).
 */
public interface Categorized {
	Category category();
}
