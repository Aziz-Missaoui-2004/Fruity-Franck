package frank.gal.aut.actions;

import engine.core.Model;
import engine.entities.Entity;
import engine.gal.aut.iGALAction;

// == EXPLODE ==

/**
 * The GAL {@code Explode} action read as self-destruction
 */
public final class Explode implements iGALAction {

	public static final Explode INSTANCE = new Explode();

	private Explode() {
	}

	@Override
	public boolean exec(Entity self) {
		Model model = self.model();
		if (model != null)
			model.remove(self);
		else
			self.destroy();
		return true;
	}
}
