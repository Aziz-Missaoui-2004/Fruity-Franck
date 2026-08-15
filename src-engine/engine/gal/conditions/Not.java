package engine.gal.conditions;

import engine.entities.Entity;
import engine.gal.aut.iGALCondition;

// == NOT ==

/**
 * The GAL negation {@code not(Condition)}.
 *
 * <p>
 * Note: conditions that bind the GAL {@code $} variable (e.g. {@code Closest},
 * {@code Step}) still run their selection side effect when negated —
 * {@code not} inverts only the boolean result, matching GAL semantics.
 */
public final class Not implements iGALCondition {

	private final iGALCondition inner;

	public Not(iGALCondition inner) {
		this.inner = inner;
	}

	@Override
	public boolean eval(Entity self) {
		return !inner.eval(self);
	}
}
