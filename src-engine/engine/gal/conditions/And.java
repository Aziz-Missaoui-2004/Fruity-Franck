package engine.gal.conditions;

import engine.entities.Entity;
import engine.gal.aut.iGALCondition;

// == AND ==

/**
 * The GAL conjunction {@code C1 & C2 & …}: true iff every operand is true.
 * Short-circuits, so a later operand's {@code $}-binding side effect (e.g.
 * {@code Step}) runs only if the earlier operands held — matching left-to-right
 * GAL evaluation.
 */
public final class And implements iGALCondition {

	private final iGALCondition[] operands;

	public And(iGALCondition... operands) {
		this.operands = operands;
	}

	@Override
	public boolean eval(Entity self) {
		for (iGALCondition c : operands)
			if (!c.eval(self))
				return false;
		return true;
	}
}
