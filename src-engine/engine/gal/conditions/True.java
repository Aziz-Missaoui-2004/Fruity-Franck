package engine.gal.conditions;

import engine.entities.Entity;
import engine.gal.aut.iGALCondition;

// == TRUE ==

/** The GAL {@code True} condition: always satisfied. */
public final class True implements iGALCondition {

	/** The single shared instance — it is stateless. */
	public static final True INSTANCE = new True();

	private True() {
	}

	@Override
	public boolean eval(Entity self) {
		return true;
	}
}
