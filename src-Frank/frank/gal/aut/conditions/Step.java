package frank.gal.aut.conditions;

import engine.core.Model;
import engine.entities.Entity;
import engine.geometry.Grid;
import engine.gal.aut.Category;
import engine.gal.aut.CategoryMatcher;
import engine.gal.aut.Direction;
import engine.gal.aut.GALBot;
import engine.gal.aut.iGALCondition;

// == STEP ==

/**
 * The GAL {@code Step(Direction, [n,] Category)} condition: is there an entity
 * of the given category in the cell {@code n} steps (default 1) away in the
 * given direction?
 */
public final class Step implements iGALCondition {

	private final Direction dir;
	private final Category cat;
	private final int n;

	public Step(Direction dir, Category cat) {
		this(dir, cat, 1);
	}

	public Step(Direction dir, Category cat, int n) {
		this.dir = dir;
		this.cat = cat;
		this.n = n;
	}

	@Override
	public boolean eval(Entity self) {
		if (!(self.bot() instanceof GALBot bot))
			return false;
		Model model = self.model();
		if (model == null)
			return false;

		Direction abs = dir.resolve(Direction.ofAngle(self.orientation()));
		Grid grid = model.grid();
		Grid.Position pos = self.position();
		Grid.Position target = grid.new Position(pos.x() + abs.dx() * n, pos.y() + abs.dy() * n);
		Grid.Cell cell = grid.cellAt(target);

		if (cat == Category.Void) {
		    bot.select(null);
		    return model.canOccupy(self, target);
		}

		CategoryMatcher matcher = bot.matcher();
		if (matcher == null)
			return false;
		for (Entity other : cell.entities()) {
			if (other != self && matcher.matches(cat, self, other)) {
				bot.select(other);
				return true;
			}
		}
		bot.select(null);
		return false;
	}
}
