package frank.gal.aut.actions;

import engine.entities.Entity;
import engine.entities.Stunt;
import engine.gal.aut.DirArg;
import engine.gal.aut.Direction;
import engine.gal.aut.iGALAction;

// == TURN ==

/**
 * The GAL {@code Turn(direction)} action: reorients the entity to face the
 * resolved direction
 */
public final class Turn implements iGALAction {

	private final DirArg dir;

	public Turn(DirArg dir) {
		this.dir = dir;
	}

	@Override
	public boolean exec(Entity self) {
		Stunt stunt = self.stunt();
		if (stunt != null && stunt.busy())
			return false;
		int[] d = dir.resolve(self);
		if (d[0] != 0 || d[1] != 0)
			self.face(Direction.cardinal(d[0], d[1]).toAngle());
		return true;
	}
}
