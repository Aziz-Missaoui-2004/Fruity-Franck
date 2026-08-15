package engine.gal.aut;

import engine.entities.Entity;

// == iGALAction ==

/**
 * A GAL transition effect. Run on the entity whose automaton fired the
 * transition; an action actuates through the entity's {@code Stunt} (e.g.
 * {@code Move} pushes a velocity command). The interpretation of each verb is
 * game-specific, but {@code Move}/{@code Turn} have engine defaults.
 */
public interface iGALAction {
	boolean exec(Entity e);
}
