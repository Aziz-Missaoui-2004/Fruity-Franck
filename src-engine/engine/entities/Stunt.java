package engine.entities;

import engine.core.Model;
import engine.geometry.ISU;

// == STUNT ==

public abstract class Stunt {

	// FIELDS

	/** The entity this stunt moves. */
	protected final Entity owner;

	// The commanded velocity is NOT stored on the stunt: it lives on the owning
	// {@link Entity} (its {@code linearSpeed} / {@code angularSpeed_degree}). The
	// stunt is simply the party that writes those fields (from bot commands or
	// actions) and reads them back when it actuates motion in {@link #move}.

	// BUSY (timed/blocking actions: Wait, Jump, Hit)

	/**
	 * Motion ticks left during which the actuator is occupied by a timed action.
	 */
	private int busyTicks;
	/**
	 * Set while a {@code Wait} is in progress, so its completion can be signalled
	 * once.
	 */
	private boolean waiting;

	/**
	 * @return whether a timed action currently occupies the actuator. While busy, a
	 *         GAL action that would redirect motion (e.g. {@code Move}) reports
	 *         failure so the FSM holds its state until the action completes.
	 */
	public boolean busy() {
		return busyTicks > 0;
	}

	/**
	 * Occupy the actuator for {@code ticks} motion ticks (takes the longer of the
	 * two).
	 */
	public void setBusy(int ticks) {
		if (ticks > busyTicks)
			busyTicks = ticks;
	}

	/** Begin a {@code Wait} of {@code ticks} motion ticks */
	public void beginWait(int ticks) {
		busyTicks = Math.max(0, ticks);
		waiting = true;
	}

	/**
	 * @return {@code true} exactly once, when an armed {@code Wait} has elapsed.
	 */
	public boolean consumeWaitDone() {
		if (waiting && busyTicks <= 0) {
			waiting = false;
			return true;
		}
		return false;
	}

	// CONSTRUCTOR

	protected Stunt(Entity owner) {
		this.owner = owner;
	}

	// COLLISION

	/**
	 * Notify the actuator that its entity collided with {@code impactor}, so it can
	 * react at the motion level (e.g. stop dead rather than keep pushing). Wired by
	 * {@link Model#resolveCollisions()} after the game's outcome is applied.
	 * Default no-op; subclasses override to interrupt motion on impact.
	 */
	public void collision(Entity impactor) {
	}

	// COMMAND (pushed by the bot, stored on the owning entity)

	/**
	 * Push a linear velocity command (cm per tick) by writing the owner's
	 * {@code linearSpeed}, leaving its rotation unchanged. The stunt actuates it on
	 * each {@link #move}; the bot calls this when its decision changes.
	 */
	public void drive(double vx, double vy) {
		owner.setLinearSpeed(owner.center().getISU().new Vector(vx, vy));
	}

	// DEFERRED COMMAND (extension seam)

	/**
	 * Ask the stunt to honour a command later rather than now
	 */
	public void request(double vx, double vy) {
	}

	/** Drop any deferred command. No-op unless a subclass buffers one. */
	public void clearRequest() {
	}

	/**
	 * @return the owner's current commanded velocity (cm per tick), or
	 *         {@code null}.
	 */
	public ISU.Vector velocity() {
		return owner.getLinearSpeed();
	}

	/**
	 * Push a full motion command by writing the owner's {@code linearSpeed} (cm per
	 * tick) and {@code angularSpeed_degree} (degrees per tick).
	 */
	public void drive(double vx, double vy, double omegaDegPerTick) {
		drive(vx, vy);
		owner.setAngularSpeed_degree(omegaDegPerTick);
	}

	// SIMULATION (sub-stepped: see Model.update)

	/**
	 * The largest fraction of a cell an entity may move in a single micro-step. The
	 * cap makes both the half-cell leading-edge wall probe and the per-micro
	 * collision test exact: nothing can skip a one-cell wall or cross another
	 * entity between micro-steps.
	 */
	protected static final double MAX_STEP_FRACTION = 0.5;

	/**
	 * Once-per-tick preparation run before the micro-step loop (e.g. continuous
	 * rotation and applying a buffered turn). The base implementation counts down
	 * the {@link #busy() busy} timer of timed actions; subclasses that override
	 * must call {@code super.beginTick(model)}.
	 */
	public void beginTick(Model model) {
		if (busyTicks > 0)
			busyTicks--;
	}

	/**
	 * @return how many equal micro-steps this stunt needs this tick so no micro
	 *         exceeds {@link #MAX_STEP_FRACTION} of a cell. {@code 1} for an entity
	 *         moving at most half a cell per tick (the common case), so a world of
	 *         slow entities runs exactly as a single full step.
	 */
	public int substeps() {
		ISU.Vector v = owner.getLinearSpeed();
		if (v == null)
			return 1;
		double maxComp = Math.max(Math.abs(v.x()), Math.abs(v.y()));
		double cap = MAX_STEP_FRACTION * owner.center().getISU().getCmPerCell();
		return Math.max(1, (int) Math.ceil(maxComp / cap));
	}

	/**
	 * Advance motion by micro-step {@code i} of {@code n}, in lockstep with the
	 * rest of the model (the caller resolves collisions after each micro-step).
	 * Default no-op (a static stunt never moves).
	 */
	public void advance(Model model, int i, int n) {
	}
}
