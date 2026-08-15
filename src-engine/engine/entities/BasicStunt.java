package engine.entities;

import engine.core.Model;
import engine.geometry.ISU;

// == BASIC STUNT ==

/**
 * A simple stunt that actuates the bot's pushed command: each tick it rotates
 * by the commanded angular velocity and translates by the commanded linear
 * velocity, provided the destination cell is legal ({@link Model#canOccupy}).
 *
 * <p>
 * It also implements the {@code Stunt} deferred-command seam for grid turn
 * buffering: a perpendicular turn requested mid-cell ({@link #request}) is held
 * in a single slot and applied at the next crossing, snapping the entity onto
 * the new lane's centre line. A direct {@link #drive} cancels any pending turn.
 *
 * <p>
 * Game-agnostic engine plumbing: the only game knowledge it consults is the
 * {@link Model#canOccupy} predicate (what counts as a wall), so it is reusable
 * by any grid-movement game without subclassing.
 */
public class BasicStunt extends Stunt {

	// Single-slot buffered turn (only the most recent request is kept).
	private double reqVx, reqVy;
	private boolean hasReq;
	// Stop-at-centre request (set on key release): coast to the next lane centre in
	// the CURRENT travel direction and halt there. The target is derived from the
	// live direction on each step, so it follows a buffered turn (Frank stops at the
	// next cell in the direction he ends up going) instead of being pinned to the
	// direction held when the key was released.
	private boolean stopPending = false;

	public BasicStunt(Entity owner) {
		super(owner);
	}

	// DEFERRED COMMAND (turn buffering)

	@Override
	public void request(double vx, double vy) {
		reqVx = vx;
		reqVy = vy;
		hasReq = true;
		stopPending = false; // a fresh steering press supersedes a pending stop
	}

	@Override
	public void clearRequest() {
		hasReq = false;
	}

	@Override
	public void drive(double vx, double vy) {
		stopPending = false; // a fresh user command supersedes a pending stop
		setVelocity(vx, vy);
	}

	/**
	 * Set the active velocity without touching a pending stop. Used when the engine
	 * itself changes direction (applying a buffered turn): a turn must not cancel
	 * the player's pending stop, otherwise a tapped perpendicular key would leave
	 * Frank moving forever in the new direction.
	 */
	private void setVelocity(double vx, double vy) {
		clearRequest(); // a direct command supersedes any buffered turn
		super.drive(vx, vy);
	}

	/**
	 * Set when a micro-step hit a wall this tick, latching the rest of the tick.
	 */
	private boolean blocked;

	@Override
	public void beginTick(Model model) {
		super.beginTick(model);
		blocked = false;
		double omega = owner.getAngularSpeed_degree();
		if (omega != 0)
			owner.turn((int) Math.round(omega));

		applyBufferedTurnAtCrossing(model);
	}

	@Override
	public void advance(Model model, int i, int n) {
		if (blocked)
			return;

		ISU.Vector speed = owner.getLinearSpeed();
		if (speed == null || (speed.x() == 0 && speed.y() == 0))
			return;
		double sx = speed.x();
		double sy = speed.y();

		ISU isu = owner.center().getISU();
		double mx = (i < n - 1) ? sx / n : sx - sx / n * (n - 1);
		double my = (i < n - 1) ? sy / n : sy - sy / n * (n - 1);
		if (mx == 0 && my == 0)
			return;
		ISU.Vector micro = isu.new Vector(mx, my);

		// Capture the stop target from the position BEFORE the step, in the current
		// travel direction: this honours a buffered turn already applied this tick,
		// and pins the centre being approached so the one just passed is clamped to
		// rather than skipped.
		ISU.Coord stopCentre = stopPending ? nextCentreAhead(micro) : null;

		if (stepHitsWall(model, micro)) {
			snapToLaneCellAlong(micro);
			blocked = true;
			return;
		}

		owner.translate(micro);

		if (stopCentre != null && reachedCentre(stopCentre, micro)) {
			clampOntoCentre(stopCentre, micro);
			drive(0, 0); // halt (clears the stop flag)
			blocked = true;
		}
	}

	/** @return the next cell centre at or ahead of the current position along {@code step}. */
	private ISU.Coord nextCentreAhead(ISU.Vector step) {
		ISU.Coord c = owner.center();
		ISU isu = c.getISU();
		double cm = isu.getCmPerCell();
		boolean alongX = step.x() != 0;
		double dir = Math.signum(alongX ? step.x() : step.y());
		double pos = alongX ? c.x() : c.y();
		// Cell centres sit at (k + 0.5) * cm. The Coord constructor normalizes the
		// value back into the world, handling a torus wrap past the seam.
		double k = pos / cm - 0.5;
		double targetK = dir > 0 ? Math.ceil(k - 1e-9) : Math.floor(k + 1e-9);
		double targetVal = (targetK + 0.5) * cm;
		return alongX ? isu.new Coord(targetVal, c.y()) : isu.new Coord(c.x(), targetVal);
	}

	/**
	 * @return whether the centre has been reached or passed along {@code step}
	 *         (torus-aware: the signed remaining distance flips once passed).
	 */
	private boolean reachedCentre(ISU.Coord centre, ISU.Vector step) {
		boolean alongX = step.x() != 0;
		double dir = Math.signum(alongX ? step.x() : step.y());
		ISU.Vector delta = owner.center().nearestDeltaTo(centre);
		double d = alongX ? delta.x() : delta.y();
		return dir > 0 ? d <= 1e-9 : d >= -1e-9;
	}

	/** Clamp the travel-axis coordinate exactly onto {@code centre} (a pull-back, never forward). */
	private void clampOntoCentre(ISU.Coord centre, ISU.Vector step) {
		ISU.Coord here = owner.center();
		boolean alongX = step.x() != 0;
		double nx = alongX ? centre.x() : here.x();
		double ny = alongX ? here.y() : centre.y();
		owner.setCoord(here.getISU().new Coord(nx, ny));
	}

	// WALL CLEARANCE

	/**
	 * @return whether taking {@code step} would push any part of the body into a
	 *         wall. Probes the leading edge — the centre plus half the cell along
	 *         the travel axis — nudged inward by an epsilon so an edge that merely
	 *         <i>touches</i> the wall boundary (the lane-aligned rest position)
	 *         stays legal. The perpendicular axis keeps the centre, since a
	 *         lane-centred entity never spans more than its own row/column there.
	 */
	private boolean stepHitsWall(Model model, ISU.Vector step) {
		ISU.Coord c = owner.center();
		ISU isu = c.getISU();
		double reach = isu.getCmPerCell() / 2.0 - 1e-6;
		double sx = Math.signum(step.x());
		double sy = Math.signum(step.y());
		double px = c.x() + step.x() + sx * reach;
		double py = c.y() + step.y() + sy * reach;
		ISU.Coord probe = isu.new Coord(px, py);
		return !model.canOccupy(owner, probe.toGridPosition());
	}

	/**
	 * Snap the travel-axis coordinate onto the current lane cell's centre, leaving
	 * the perpendicular axis untouched, so the body rests flush against the wall.
	 */
	private void snapToLaneCellAlong(ISU.Vector step) {
		ISU.Coord here = owner.center();
		ISU.Coord cellCenter = owner.position().toISUCoordCentered();
		double nx = step.x() != 0 ? cellCenter.x() : here.x();
		double ny = step.y() != 0 ? cellCenter.y() : here.y();
		owner.setCoord(here.getISU().new Coord(nx, ny));
	}

	// BUFFERED TURN

	/**
	 * If a perpendicular turn is buffered, apply it once the entity is aligned on a
	 * crossing — i.e. its position along the <i>current</i> travel axis is at (or
	 * within half a step of) a cell centre. A stopped entity turns immediately.
	 * When applied, the travel-axis coordinate is snapped exactly onto the cell
	 * centre so the entity sits on the new lane's centre line, and the buffered
	 * command becomes the active one.
	 *
	 * <p>
	 * The turn is only taken if the target cell is legal ({@link Model#canOccupy}):
	 * a buffered turn into a wall is held, not spent, so the entity keeps going and
	 * turns at the next crossing where that direction opens up.
	 */
	private void applyBufferedTurnAtCrossing(Model model) {
		if (!hasReq)
			return;

		ISU.Vector active = owner.getLinearSpeed();
		boolean stopped = active == null || (active.x() == 0 && active.y() == 0);
		if (stopped) {
			if (turnIsLegal(model)) // not moving: turn now if the way is clear
				snapAndApply(active);
			return;
		}

		boolean travellingX = active.x() != 0;
		ISU.Coord here = owner.center();
		ISU.Coord cellCenter = owner.position().toISUCoordCentered();
		double offset = travellingX ? here.x() - cellCenter.x() : here.y() - cellCenter.y();
		double halfStep = Math.abs(travellingX ? active.x() : active.y()) / 2.0 + 1e-9;

		if (Math.abs(offset) <= halfStep && turnIsLegal(model))
			snapAndApply(active);
	}

	/**
	 * @return whether the cell one step in the buffered direction can be entered.
	 */
	private boolean turnIsLegal(Model model) {
		double cmPerCell = owner.center().getISU().getCmPerCell();
		ISU.Coord cellCenter = owner.position().toISUCoordCentered();
		ISU.Vector step = cellCenter.getISU().new Vector(Math.signum(reqVx) * cmPerCell,
				Math.signum(reqVy) * cmPerCell);
		return model.canOccupy(owner, cellCenter.mkTranslated(step).toGridPosition());
	}

	/**
	 * Snap the travel-axis coordinate to the cell centre, then activate the turn.
	 */
	private void snapAndApply(ISU.Vector active) {
		ISU.Coord here = owner.center();
		ISU.Coord cellCenter = owner.position().toISUCoordCentered();
		double nx = here.x();
		double ny = here.y();
		if (active != null && active.x() != 0)
			nx = cellCenter.x(); // travelling east/west -> snap x onto the new vertical lane
		else if (active != null && active.y() != 0)
			ny = cellCenter.y(); // travelling north/south -> snap y onto the new horizontal lane
		owner.setCoord(here.getISU().new Coord(nx, ny));

		double vx = reqVx;
		double vy = reqVy;
		setVelocity(vx, vy); // sets the new direction but keeps any pending stop alive
	}

	/**
	 * Request that the entity coasts to the next lane centre and halts there. The
	 * centre is resolved per micro-step from the live travel direction (see
	 * {@link #advance}/{@link #nextCentreAhead}) and reached by normal motion, so
	 * releasing the key grants no free forward distance and the stop correctly
	 * follows a buffered turn. If already stopped, does nothing.
	 */
	public void requestStopAtNextCell() {
		if (owner == null)
			return;
		ISU.Vector speed = owner.getLinearSpeed();
		if (speed == null || (speed.x() == 0 && speed.y() == 0))
			return; // not moving – nothing to do
		stopPending = true;
	}
}
