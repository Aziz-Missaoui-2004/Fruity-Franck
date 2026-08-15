package game;

import engine.core.Game;
import engine.core.Model;
import engine.entities.Entity;
import engine.geometry.Grid;
import engine.graphics.Avatar;
import game.entities.Pac;
import game.graphics.PacManAvatar;


// == PACMAN MODEL ==

/**
 * The Pac-Man world. The engine detects collisions; this model decides what
 * they <i>mean</i> by delegating to {@link Rules} (the collision matrix) over
 * the {@link GameState} (score / flags) — keeping all game knowledge out of the
 * engine and the outcome rules out of an {@code instanceof} chain.
 */
public class PacManModel extends Model {

	private final GameState state = new GameState();
	private final Rules rules = new Rules();
	private Maze maze; // optional: walls for the demo arena (null = open world)
	private Pac player; // optional: the keyboard Pac, so power can be cleared on timeout

	public PacManModel(Game game) {
		super(game);
	}

	public PacManModel(Game game, Maze maze) {
		super(game);
		this.maze = maze;
	}

	/** Register the player Pac so frightened-mode timeout can drop its power-up. */
	public void setPlayer(Pac player) {
		this.player = player;
	}

	// SIMULATION

	/**
	 * One motion tick, plus the frightened-mode countdown: when the power pellet's
	 * timer runs out, Pac loses its power-up (the ghost stops being edible). Motion
	 * and collisions are the base {@link Model#update()}.
	 */
	@Override
	public void update() {
		super.update();
		if (!state.tickFrightened() && player != null)
			player.setPowered(false);
		syncGhostMode();
	}

	/**
	 * Push the frightened state onto the ghosts' avatars (via the two-way
	 * entity&harr;avatar link) so their look follows the power-pellet timer. This
	 * is a stopgap: once the ghost runs a GAL automaton, its {@code Flee} state's
	 * mode will drive the avatar and this can go.
	 */
	private void syncGhostMode() {
		String mode = state.frightened() ? PacManAvatar.FRIGHTENED : Avatar.DEFAULT;
		for (Entity e : entities()) {
			if (Category.of(e) != Category.GHOST)
				continue;
			Avatar a = e.getAvatar();
			if (a != null)
				a.setMode(mode);
		}
	}

	// PHYSICS

	@Override
	public boolean canOccupy(Entity e, Grid.Position target) {
		// Walls block movement (the world is a torus, so positions never go
		// out-of-bounds); with no maze the whole world is open.
		return maze == null || !maze.isWall(target.x(), target.y());
	}

	// COLLISION OUTCOMES

	@Override
	protected void onCollision(Entity a, Entity b) {
		rules.resolve(this, state, a, b);
	}

	// GETTERS

	public int score() {
		return state.score();
	}

	public boolean pacAlive() {
		return state.pacAlive();
	}

	/** @return the mutable game state (score, flags, events). */
	public GameState state() {
		return state;
	}
}
