package frank.gal.aut.conditions;

import engine.core.Model;
import engine.entities.Entity;
import engine.geometry.Grid;
import engine.gal.aut.Category;
import engine.gal.aut.CategoryMatcher;
import engine.gal.aut.GALBot;
import engine.gal.aut.iGALCondition;

// == GOT ==

/**
 * The Fruity Frank GAL {@code Got(Category)} condition: true iff an entity
 * matching {@code Category} (per the bot's {@link CategoryMatcher}) currently
 * <i>overlaps</i> the asking entity's bounding. On success the matched entity is
 * bound to the bot's {@code $} variable; on failure {@code $} is cleared.
 *
 * <p>
 * It uses bounding intersection ({@link Entity#intersects}), not mere same-cell
 * membership, to preserve the collision fidelity of the imperative passes it is
 * meant to replace (pickup, crush, projectile-hit, dig). Candidates are limited
 * to the cells the asker's hitbox occupies — an intersecting body must share at
 * least one of those cells — so the test stays cheap (no O(n) world scan).
 *
 * <p>
 * Wired to the spare {@code Got} keyword in {@code GalCompiler} (no parser
 * change).
 */
public final class Got implements iGALCondition {

	private final Category cat;

	public Got(Category cat) {
		this.cat = cat;
	}

	@Override
	public boolean eval(Entity self) {
		if (!(self.bot() instanceof GALBot bot))
			return false;
		CategoryMatcher matcher = bot.matcher();
		Model model = self.model();
		if (matcher == null || model == null)
			return false;

		for (Grid.Cell cell : self.getHitbox().occupiedCells()) {
			for (Entity other : cell.entities()) {
				if (other == self || !matcher.matches(cat, self, other))
					continue;
				if (self.intersects(other)) {
					bot.select(other); // $ = the matched entity
					return true;
				}
			}
		}
		bot.select(null);
		return false;
	}
}
