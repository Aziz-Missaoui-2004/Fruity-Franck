package engine.gal.aut;

import engine.entities.Entity;

// == DIR ARG ==

/**
 * A direction <i>expression</i> evaluated at action time: it may be a fixed
 * absolute {@link Direction}, the selected entity's direction ({@code $.d}), or
 * its opposite ({@code Opp($.d)}). Resolving needs the acting entity (to read
 * its bot's {@code $} selection and its position), which a plain
 * {@link Direction} constant cannot carry — hence this small abstraction.
 *
 * @return a unit step {@code {dx, dy}} in screen coordinates (components in
 *         {@code {-1, 0, 1}}); {@code {0, 0}} when undefined.
 */
@FunctionalInterface
public interface DirArg {
	int[] resolve(Entity self);
}
