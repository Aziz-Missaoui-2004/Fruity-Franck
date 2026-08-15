package engine.gal.conditions;

import engine.entities.Entity;
import engine.gal.aut.iGALCondition;

// == OR ==

/**
 * The GAL disjunction {@code C1 / C2 / …}: true iff some operand is true.
 * Short-circuits on the first satisfied operand.
 */
public final class Or implements iGALCondition {

	private final iGALCondition[] operands;

	public Or(iGALCondition... operands) {
		this.operands = operands;
	}

	@Override
	public boolean eval(Entity self) {
		for (iGALCondition c : operands)
			if (c.eval(self))
				return true;
		return false;
	}
}
