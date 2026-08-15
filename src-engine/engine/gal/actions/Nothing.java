package engine.gal.actions;

import engine.entities.Entity;
import engine.gal.aut.iGALAction;

// == NOTHING ==

/**
 * The GAL no-op action, used for action-less transitions
 * {@code | cond ? :(tgt)}. It performs nothing and always reports success, so
 * such a transition fires purely on its condition (the boolean-{@code exec}
 * contract: a transition is taken iff its condition holds <i>and</i> its action
 * succeeds).
 */
public final class Nothing implements iGALAction {

	/** The single shared instance — it is stateless. */
	public static final Nothing INSTANCE = new Nothing();

	private Nothing() {
	}

	@Override
	public boolean exec(Entity self) {
		return true;
	}
}
