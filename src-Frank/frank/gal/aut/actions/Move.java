package frank.gal.aut.actions;

import engine.entities.Entity;
import engine.entities.Stunt;
import engine.gal.aut.DirArg;
import engine.gal.aut.Direction;
import engine.gal.aut.GALBot;
import engine.gal.aut.iGALAction;
import engine.geometry.ISU;

// == MOVE ==

/**
 * The GAL {@code Move(direction)} action.
 */
public final class Move implements iGALAction {

	private final DirArg dir;

	public Move(DirArg dir) {
		this.dir = dir;
	}

	@Override
	public boolean exec(Entity self) {
		if (!(self.bot() instanceof GALBot bot))
			return false;

		Stunt stunt = bot.stunt();
		if (stunt == null || stunt.busy())
			return false;

		int[] d = dir.resolve(self);
		if (d[0] == 0 && d[1] == 0)
			return false;

		double speed = bot.speed();
		double vx = d[0] * speed;
		double vy = d[1] * speed;

		ISU.Vector current = self.getLinearSpeed();

		if (isPerpendicularTurn(current, vx, vy)) {
			stunt.request(vx, vy);
			return true;
		}

		self.face(Direction.cardinal(d[0], d[1]).toAngle());
		stunt.drive(vx, vy);
		return true;
	}

	private boolean isPerpendicularTurn(ISU.Vector current, double vx, double vy) {
		if (current == null)
			return false;

		double cx = current.x();
		double cy = current.y();

		if ((cx == 0 && cy == 0) || (vx == 0 && vy == 0))
			return false;

		boolean currentlyHorizontal = cx != 0 && cy == 0;
		boolean currentlyVertical = cy != 0 && cx == 0;

		boolean requestedHorizontal = vx != 0 && vy == 0;
		boolean requestedVertical = vy != 0 && vx == 0;

		return (currentlyHorizontal && requestedVertical) || (currentlyVertical && requestedHorizontal);
	}
}