package game;

import java.awt.Dimension;

import engine.ai.Brain;
import engine.core.Controller;
import engine.core.Ticker;
import engine.debug.Debug;
import engine.entities.BasicStunt;
import engine.entities.Entity;
import engine.gal.aut.GALBot;
import engine.graphics.Camera;
import engine.graphics.Minimap;
import game.ai.PacManAutomata;
import game.ai.PacManCategories;
import game.bots.KeyboardBot;
import game.entities.Ghost;
import game.entities.Gum;
import game.entities.Pac;
import game.graphics.PacManPainter;
import game.graphics.PacManView;
import game.graphics.Sprites;

import oop.graphics.Canvas;
import oop.tasks.Runnable;
import oop.tasks.Runtime;

// == GAME APP ==

/**
 * Sprite demo. Boots the {@code oop} runtime, builds the {@link Maze} arena
 * (walls, gums, power pellets, spawns) and runs the engine's cooperating loops:
 *
 * <ul>
 * <li>a motion {@link Ticker} → {@code Model.update()} (move + collide +
 * frightened countdown),</li>
 * <li>an AI {@link Ticker} → {@link Brain} → {@code GALBot.tick()} (the chasing
 * ghost runs the Tracking automaton),</li>
 * <li>a low-rate respawn {@link Ticker} so the demo runs forever,</li>
 * <li>a repaint pump → {@link PacManView} drawing the {@link Sprites}.</li>
 * </ul>
 *
 * It showcases: keyboard control with turn-buffering at crossings, walls
 * blocking movement (and dropping a buffered turn into a wall), the GAL ghost
 * chasing Pac around obstacles and through the torus edges, gum eating +
 * scoring, and a power pellet flipping the ghost to frightened-blue so Pac can
 * eat it.
 */
public class GameApp {

	private static final int MOTION_TPS = 30; // physics cadence
	private static final int AI_TPS = 8; // brain cadence
	private static final int SPAWN_TPS = 4; // respawn-watchdog cadence
	private static final int RENDER_MS = 33; // ~30 fps

	private static final int VIEW_COLS = 19; // viewport size in cells (world is larger)
	private static final int VIEW_ROWS = 19;
	private static final int MINIMAP_PX = 160; // minimap panel size

	private static final double PAC_SPEED = 0.2; // cm per motion tick
	static final double GHOST_SPEED = 0.15;

	/**
	 * Liveness flag for the self-reposting loops. Flipped to {@code false} when the
	 * window closes so the {@link RenderLoop} stops posting repaints — the
	 * {@code oop} runtime nulls its canvas on close, and a repaint after that would
	 * NPE.
	 */
	private static volatile boolean running = true;

	public static void main(String[] args) {
		Maze maze = new Maze();
		PacMan game = new PacMan(maze.cols(), maze.rows(), true, true, 1.0, 32); // 32 px/cell
		PacManModel model = new PacManModel(game, maze);
		Sprites sprites = new Sprites();

		for (Maze.Cell c : maze.gums())
			model.add(new Gum(game, c.col(), c.row()));
		for (Maze.Cell c : maze.pellets())
			model.add(new Gum(game, c.col(), c.row(), true));

		KeyboardBot pacBot = spawnPac(model, game, maze);
		Controller controller = new Controller(pacBot); // engine listener -> player bot
		spawnGhost(model, game, maze);

		int cellPx = (int) Math.round(game.getCmPerCell() * game.getPixelPerCm());
		// The window is a viewport smaller than the world, so the camera scrolls.
		int pxW = VIEW_COLS * cellPx;
		int pxH = VIEW_ROWS * cellPx;

		PacManView view = new PacManView(game, model, sprites);
		Camera camera = new Camera(game.getWidth_cm(), game.getHeight_cm(), game.torusOnX(), game.torusOnY(), pxW, pxH,
				game.getPixelPerCm());
		view.setCamera(camera); // follow the player Pac (PacManView updates it each frame)

//		Minimap minimap = new Minimap(game.getWidth_cm(), game.getHeight_cm(), pxW - MINIMAP_PX - 8, 8, MINIMAP_PX,
//				MINIMAP_PX);
		PacManPainter painter = new PacManPainter(view, maze, cellPx, game.getCmPerCell(), null);
		Brain brain = new Brain(model);

		// Opt-in engine debug overlays. Everything is OFF by default; flip the setters
		// you want on. Injected into the painter (HUD + per-entity overlays) and the
		// motion ticker (inter-tick timing).
		Debug debug = new Debug();
		debug.setPixelPerCm(game.getPixelPerCm()); // fallback mapping when no camera
		 debug.setShowTickStats(true);
		 debug.setShowPaintStats(true);
		 debug.setShowFps(true);
		 debug.setShowHitboxes(true);
		 debug.setShowStates(true);
		painter.setDebug(debug); // also forwards to the view

		Runtime.boot(new Dimension(pxW, pxH), new Runnable() {
			@Override
			public void run() {
				Canvas canvas = (Canvas) Runtime.task().find("canvas");
				canvas.set(painter);
				canvas.set((Canvas.KeyListener) controller);
				canvas.set((Canvas.MouseListener) controller);

				Ticker motionTicker = new Ticker(MOTION_TPS);
				motionTicker.setDebug(debug); // inter-tick timing on the simulation
				motionTicker.start(model); // Ticker -> Model.update()
				new Ticker(AI_TPS).start(brain); // Brain -> ghost.think()
				new Ticker(SPAWN_TPS).start(new Spawner(model, game, maze, controller));

				Runtime.post(new RenderLoop(canvas), RENDER_MS);
			}
		}, true);
	}

	// SCENE BUILDERS

	/**
	 * Spawn the keyboard Pac at its maze spawn; returns its bot (the key listener).
	 */
	private static KeyboardBot spawnPac(PacManModel model, PacMan game, Maze maze) {
		Pac pac = new Pac("Pac", game, maze.pacSpawn().col(), maze.pacSpawn().row());
		pac.setStunt(new BasicStunt(pac));
		KeyboardBot bot = new KeyboardBot(pac, PAC_SPEED);
		pac.setBot(bot);
		model.add(pac);
		model.setPlayer(pac);
		return bot;
	}

	/**
	 * Spawn the chasing GAL ghost (runs the Tracking automaton) at its maze spawn.
	 */
	private static void spawnGhost(PacManModel model, PacMan game, Maze maze) {
		Ghost ghost = new Ghost("Blinky", game, maze.ghostSpawn().col(), maze.ghostSpawn().row());
		ghost.setStunt(new BasicStunt(ghost));
		ghost.setBot(new GALBot(ghost, PacManAutomata.tracking(), GHOST_SPEED, new PacManCategories()));
		model.add(ghost);
	}

	/**
	 * Keeps exactly one Pac and one ghost in the world: when either is eaten it is
	 * respawned at its starting cell. A fresh Pac brings a fresh
	 * {@link KeyboardBot}; rather than re-registering a canvas listener, the
	 * persistent {@link Controller} is simply pointed at the new bot so controls
	 * keep working.
	 */
	private static class Spawner implements Ticker.TickListener {
		private final PacManModel model;
		private final PacMan game;
		private final Maze maze;
		private final Controller controller;

		Spawner(PacManModel model, PacMan game, Maze maze, Controller controller) {
			this.model = model;
			this.game = game;
			this.maze = maze;
			this.controller = controller;
		}

		@Override
		public void onTick(double deltaTime) {
			boolean hasPac = false, hasGhost = false;
			for (Entity e : model.entities()) {
				Category cat = Category.of(e);
				if (cat == Category.PAC)
					hasPac = true;
				else if (cat == Category.GHOST)
					hasGhost = true;
			}
			if (!hasPac)
				controller.setPlayerBot(spawnPac(model, game, maze)); // point the listener at the new bot
			if (!hasGhost)
				spawnGhost(model, game, maze);
		}
	}

	/** Self-reposting repaint pump, running on the boot/canvas task. */
	private static class RenderLoop implements Runnable {
		private final Canvas canvas;

		RenderLoop(Canvas canvas) {
			this.canvas = canvas;
		}

		@Override
		public void run() {
			if (!running) // window closed: the canvas is gone, repainting would NPE
				return;
			canvas.repaint();
			Runtime.post(this, RENDER_MS);
		}
	}
}
