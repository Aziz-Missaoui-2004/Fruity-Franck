package engine.gal.aut;

import engine.entities.Entity;
import engine.geometry.Grid;

// == ENTITY FACTORY ==

/**
 * The per-game seam that lets GAL actions create entities without the engine
 * knowing any concrete entity class. GAL's {@code Egg}/{@code Throw} ask the
 * factory to build an entity of a requested {@link Category} at a target cell;
 * the game's implementation constructs it and wires whatever it needs
 * (bounding, a stunt, a bot with the right matcher/factory bindings) so spawned
 * entities behave like authored ones.
 *
 * <p>
 * The factory only <i>constructs and positions</i> the entity; the calling
 * action adds it to the model. Returning {@code null} means "this game makes
 * nothing for that category" (the action then reports failure).
 */
public interface EntityFactory {

	/**
	 * @param category the GAL category to create, or {@code null} for the creator's
	 *                 default offspring (e.g. {@code Egg(F)} with no category)
	 * @param at       the cell the new entity should occupy
	 * @param creator  the entity whose action is spawning this one (for
	 *                 orientation, ownership, team, …)
	 * @return the new entity positioned at {@code at}, or {@code null}
	 */
	Entity create(Category category, Grid.Position at, Entity creator);
}
