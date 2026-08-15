package engine.entities;

import engine.gal.aut.State;

// == BOT ==

/**
 * Decision-maker for an {@link Entity}. Per the GAL spec a bot is created for a
 * specific entity, carries an FSM {@link State} (whose {@code mode} also names
 * the avatar to show) and a {@code healthPercent}, and is woken periodically
 * via {@link #tick(double)} to choose its next action; {@link #collision} tells
 * it a move was interrupted. It reaches its actuator through the entity
 * ({@link #stunt()}), so there is no separate bot&harr;stunt link to maintain.
 *
 * <p>
 * Keyboard / mouse bots additionally receive raw input forwarded by the engine
 * {@code Controller} through the
 * {@code pressed}/{@code released}/{@code mouse*} hooks. These use primitive
 * signatures, so the engine entity layer keeps no UI (oop) dependency.
 */
public abstract class Bot {

	/**
	 * The entity this bot drives. Severed by {@link #detach()} on the entity's
	 * destruction, so a still-referenced bot (e.g. a controller's player bot)
	 * cannot keep a dead entity — and its stunt — alive.
	 */
	protected Entity entity;

	/** Current FSM state; its {@code mode} also names the avatar to show. */
	protected State state;

	/**
	 * 0..100; game-specific health/energy that GAL {@code Got}/{@code Add} read.
	 */
	protected int healthPercent = 100;

	public Bot(Entity entity) {
		this.entity = entity;
	}

	// LINKS

	public Entity entity() {
		return entity;
	}

	/** @return the entity's actuator, or {@code null} if detached / not yet set. */
	public Stunt stunt() {
		return entity == null ? null : entity.stunt();
	}

	/**
	 * Sever the entity link so a retained bot does not keep a dead entity alive.
	 */
	public void detach() {
		this.entity = null;
	}

	// STATE

	public State state() {
		return state;
	}

	public void state(State state) {
		this.state = state;
	}

	// ACTIVATION

	/**
	 * Wake the bot so it can choose or continue its action. Default no-op; FSM bots
	 * override it (a GAL bot steps its automaton). Driven by the {@code Brain}.
	 *
	 * @param elapsed time since the previous activation (unit set by the Brain; the
	 *                GAL automaton ignores it).
	 */
	public void tick(double elapsed) {
	}

	/**
	 * Notify the bot that its motion was interrupted by a collision with
	 * {@code impactor}: it should abort the current action and may choose a new
	 * one, now or later. Default no-op.
	 */
	public void collision(Entity impactor, double elapsed) {
	}

	// INPUT HOOKS (forwarded by the Controller; primitive signatures)

	public void pressed(int keyCode, char keyChar) {
	}

	public void released(int keyCode, char keyChar) {
	}

	public void mouseMoved(int x, int y) {
	}

	public void mousePressed(int button, int x, int y) {
	}

	public void mouseReleased(int button, int x, int y) {
	}
}
