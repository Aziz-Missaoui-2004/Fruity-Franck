package frank.gal.aut.conditions;

import engine.entities.Entity;
import engine.gal.aut.Direction;
import engine.gal.aut.iGALCondition;

// == MY DIR ==

/**
 * The GAL {@code MyDir(Direction)} condition: true when the entity is currently
 * facing the given direction. A relative argument is resolved against the
 * current facing, an absolute one is compared to the entity's cardinal facing.
 */
public final class MyDir implements iGALCondition {

	private final Direction dir;

	public MyDir(Direction dir) {
		this.dir = dir;
	}

	@Override
	public boolean eval(Entity self) {
		Direction facing = Direction.ofAngle(self.orientation());
		return facing == dir.resolve(facing);
	}
}
