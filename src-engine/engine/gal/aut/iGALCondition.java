package engine.gal.aut;

import engine.entities.Entity;

// == iGALCondition ==

/**
 * A GAL transition guard. Evaluated against the entity whose automaton is being
 * stepped; a {@code true} result lets the transition fire. Conditions that
 * select an entity (e.g. {@code Closest}, {@code Step}) also bind the GAL
 * {@code $} variable on the entity's {@link GALBot} as a side effect.
 */
public interface iGALCondition {
	boolean eval(Entity e);
}
