package frank.gal.aut.actions;

import engine.core.Model;
import engine.entities.Entity;
import engine.geometry.Grid;
import engine.gal.aut.Category;
import engine.gal.aut.DirArg;
import engine.gal.aut.EntityFactory;
import engine.gal.aut.GALBot;
import engine.gal.aut.iGALAction;

// == EGG ==

/**
 * The GAL {@code Egg} action: create a new entity on the cell in the given
 * direction (default resolved by the {@link DirArg}, typically {@code F}). The
 * entity is built by the game's {@link EntityFactory} 
 */
public final class Egg implements iGALAction {

	private final DirArg dir;
	private final Category category; // may be null = default offspring

	public Egg(DirArg dir) {
		this(dir, null);
	}

	public Egg(DirArg dir, Category category) {
		this.dir = dir;
		this.category = category;
	}

	@Override
	public boolean exec(Entity self) {
		if (!(self.bot() instanceof GALBot bot))
			return false;

		EntityFactory factory = bot.factory();
		Model model = self.model();
		if (factory == null || model == null)
			return false;

		int[] d = dir.resolve(self);
		Grid grid = model.grid();
		Grid.Position pos = self.position();
		Grid.Position at = grid.new Position(pos.x() + d[0], pos.y() + d[1]);

		Entity child = factory.create(category, at, self);
		if (child == null)
			return false;
		model.add(child); 
		return true;
	}
}
