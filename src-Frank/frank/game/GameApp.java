package frank.game;

import java.awt.Dimension;

import engine.ai.Brain;
import engine.core.Controller;
import engine.core.Ticker;
import engine.debug.Debug;
import engine.entities.BasicStunt;
import engine.gal.aut.GALBot;
import engine.graphics.Camera;
import frank.ai.FrankCategories;
import frank.bots.FrankBot;
import frank.entities.*;
import frank.graphics.FrankPainter;
import frank.graphics.FrankSprites;
import frank.graphics.FrankView;
import oop.graphics.Canvas;
import oop.tasks.Runnable;
import oop.tasks.Runtime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import engine.entities.Entity;
import engine.gal.aut.Automaton;
import gal.ast.GalCompiler;

public class GameApp {

	private static final int MOTION_TPS = 30;
	private static final int AI_TPS = 8;
	private static final int SPAWN_TPS = 2;
	private static final int RENDER_MS = 33;

	private static int viewCols;
	private static int viewRows;
	private static double frankSpeed;
	private static double enemySpeed;

	public static Debug debug = new Debug();
	public static FrankPainter painter;

	private static volatile boolean running = true;
	public static FrankAudio audio = new FrankAudio();

	private static Ticker motionTicker;
	private static Ticker aiTicker;
	private static Ticker spawnTicker;

	public static void main(String[] args) {

		String configPath = (args.length > 0) ? args[0] : "game.cfg";
		GameConfig cfg = new GameConfig(configPath);

		viewCols = cfg.getInt("view_cols", 15);
		viewRows = cfg.getInt("view_rows", 11);
		frankSpeed = cfg.getDouble("frank_speed", 1.0);
		enemySpeed = cfg.getDouble("enemy_speed", 0.6);

		FruityFrankMap map = new FruityFrankMap(cfg);
		FruityFrank game = new FruityFrank(map.cols(), map.rows());
		FruityFrankModel model = new FruityFrankModel(game, map);

		Map<String, Automaton> galAutomata = loadGalAutomata();

		spawnEnemySpawner(model, game, map, galAutomata);

		for (FruityFrankMap.Cell c : map.grassCells()) {
			model.add(new GrassLow(game, c.col(), c.row()));
			model.add(new GrassHigh(game, c.col(), c.row()));
		}

		Fruit.FruitType[] ft = Fruit.FruitType.values();
		int ti = 0;
		for (FruityFrankMap.Cell c : map.fruitCells()) {
			Fruit fruit = new Fruit(game, c.col(), c.row(), ft[ti++ % ft.length]);
			fruit.setBot(pickupBot(fruit, galAutomata));
			model.add(fruit);
		}

		for (FruityFrankMap.Cell c : map.cherryCells()) {
			Cherry cherry = new Cherry(game, c.col(), c.row());
			cherry.setBot(pickupBot(cherry, galAutomata));
			model.add(cherry);
		}

		model.state().setTotalFruits(map.fruitCells().size() + map.cherryCells().size());

		for (FruityFrankMap.Cell c : map.appleCells()) {
			Apple apple = new Apple(game, c.col(), c.row());
			apple.setStunt(new BasicStunt(apple));
			apple.setBot(new GALBot(apple, galAutomata.get("AppleFall"), game.gravity(), new FrankCategories()));
			model.add(apple);
		}

		FrankBot frankBot = spawnFrank(model, game, map, false);
		FrankController controller = new FrankController(frankBot, model);
		controller.setRestartAction(() -> resetLevel(model, game, map, controller));

		String typesStr = cfg.getString("enemy_types", "RED,GREEN,PURPLE,ORANGE");
		String[] typeNames = typesStr.split(",");

		int ei = 0;
		for (FruityFrankMap.Cell c : map.enemySpawns()) {
			String typeName = typeNames[ei % typeNames.length].trim();
			Enemy.EnemyType type;
			try {
				type = Enemy.EnemyType.valueOf(typeName);
			} catch (IllegalArgumentException e) {
				System.err.println("[GameApp] type ennemi inconnu : " + typeName + " → RED par défaut");
				type = Enemy.EnemyType.RED;
			}
			spawnEnemy(model, game, c.col(), c.row(), type, galAutomata);
			ei++;
		}

		int cellPx = (int) Math.round(game.getCmPerCell() * game.getPixelPerCm());
		int cameraW = (viewCols + 4) * cellPx;
		int windowCols = Math.max(1, Math.min(viewCols, map.cols()) - 2);
		int windowW = windowCols * cellPx;
		int pxH = viewRows * cellPx + 24;

		FrankSprites sprites = new FrankSprites();
		FrankView view = new FrankView(game, model, sprites);
		Camera cam = new Camera(game.getWidth_cm(), game.getHeight_cm(), game.torusOnX(), game.torusOnY(), cameraW,
				pxH - 24, game.getPixelPerCm());
		cam.setDeadZoneHalfX(3.0 * game.getCmPerCell());
		cam.setWindowWidth(windowW);
		view.setCamera(cam);

		painter = new FrankPainter(view, map, model, cellPx, game.getCmPerCell());
		Brain brain = new Brain(model);
		brain.setActivationOrder(Brain.BOTTOM_FIRST);

		debug.setPixelPerCm(game.getPixelPerCm());

		Runtime.boot(new Dimension(windowW, pxH), new Runnable() {
			@Override
			public void run() {
				Canvas canvas = (Canvas) Runtime.task().find("canvas");
				canvas.set(painter);
				canvas.set((Canvas.KeyListener) controller);
				canvas.set((Canvas.MouseListener) controller);

				motionTicker = new Ticker(MOTION_TPS);
				motionTicker.start(model);

				aiTicker = new Ticker(AI_TPS);
				aiTicker.start(pauseGated(model, brain));

				spawnTicker = new Ticker(SPAWN_TPS);
				spawnTicker.start(pauseGated(model, new Spawner(model, game, map, controller, cam)));

				audio.startMusic();
				Runtime.post(new RenderLoop(canvas), RENDER_MS);
				installCloseHandler();
			}
		}, true);
	}

	public static void exitGame() {
		stopLoops();
		System.exit(0);
	}

	private static Ticker.TickListener pauseGated(FruityFrankModel model, Ticker.TickListener inner) {
		return dt -> {
			if (!model.state().isPaused())
				inner.onTick(dt);
		};
	}

	private static void stopLoops() {
		running = false;
		if (motionTicker != null)
			motionTicker.stop();
		if (aiTicker != null)
			aiTicker.stop();
		if (spawnTicker != null)
			spawnTicker.stop();
	}

	private static void installCloseHandler() {
		for (java.awt.Frame f : java.awt.Frame.getFrames()) {
			if (!(f instanceof javax.swing.JFrame jf))
				continue;
			jf.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
			jf.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent e) {
					stopLoops();
				}
			});
		}
	}

	private static void resetLevel(FruityFrankModel model, FruityFrank game, FruityFrankMap map,
			Controller controller) {
		for (Entity e : new ArrayList<>(model.entities())) {
			model.remove(e);
		}

		model.state().reset();

		Map<String, Automaton> galAutomata = loadGalAutomata();

		for (FruityFrankMap.Cell c : map.grassCells()) {
			model.add(new GrassLow(game, c.col(), c.row()));
			model.add(new GrassHigh(game, c.col(), c.row()));
		}

		Fruit.FruitType[] ft = Fruit.FruitType.values();
		int ti = 0;
		for (FruityFrankMap.Cell c : map.fruitCells()) {
			Fruit fruit = new Fruit(game, c.col(), c.row(), ft[ti++ % ft.length]);
			fruit.setBot(pickupBot(fruit, galAutomata));
			model.add(fruit);
		}

		for (FruityFrankMap.Cell c : map.cherryCells()) {
			Cherry cherry = new Cherry(game, c.col(), c.row());
			cherry.setBot(pickupBot(cherry, galAutomata));
			model.add(cherry);
		}

		model.state().setFruitsRemaining(map.fruitCells().size() + map.cherryCells().size());

		spawnEnemySpawner(model, game, map, galAutomata);

		for (FruityFrankMap.Cell c : map.appleCells()) {
			Apple apple = new Apple(game, c.col(), c.row());
			apple.setStunt(new BasicStunt(apple));
			apple.setBot(new GALBot(apple, galAutomata.get("AppleFall"), game.gravity(), new FrankCategories()));
			model.add(apple);
		}

		FrankBot frankBot = spawnFrank(model, game, map, false);
		controller.setPlayerBot(frankBot);

		Enemy.EnemyType[] types = { Enemy.EnemyType.RED, Enemy.EnemyType.GREEN, Enemy.EnemyType.PURPLE,
				Enemy.EnemyType.ORANGE };

		int ei = 0;
		for (FruityFrankMap.Cell c : map.enemySpawns()) {
			spawnEnemy(model, game, c.col(), c.row(), types[ei % types.length], galAutomata);
			ei++;
		}
	}

	/** Build the GAL bot that lets a Fruit/Cherry collect itself when Frank overlaps it. */
	private static GALBot pickupBot(Entity pickup, Map<String, Automaton> galAutomata) {
		return new GALBot(pickup, galAutomata.get("Pickup"), 0.0, new FrankCategories());
	}

	private static FrankBot spawnFrank(FruityFrankModel model, FruityFrank game, FruityFrankMap map,
			boolean invincible) {
		FruityFrankMap.Cell sp = map.frankSpawn();

		Frank frank = new Frank("Frank", game, sp.col(), sp.row());
		if (invincible)
			frank.makeInvincible();

		frank.setStunt(new BasicStunt(frank));

		FrankBot bot = new FrankBot(frank, frankSpeed);
		frank.setBot(bot);

		model.add(frank);
		model.setPlayer(frank);

		return bot;
	}

	private static void spawnEnemy(FruityFrankModel model, FruityFrank game, int x, int y, Enemy.EnemyType type,
			Map<String, Automaton> galAutomata) {
		Enemy enemy = new Enemy("Enemy_" + type + "@" + x + "," + y, game, x, y, type);
		enemy.setStunt(new BasicStunt(enemy));

		Automaton automaton = (type == Enemy.EnemyType.PURPLE) ? galAutomata.get("Fleeing")
				: galAutomata.get("Tracker");

		enemy.setBot(new GALBot(enemy, automaton, enemySpeed, new FrankCategories()));
		model.add(enemy);
	}

	private static void spawnEnemySpawner(FruityFrankModel model, FruityFrank game, FruityFrankMap map,
			Map<String, Automaton> galAutomata) {

		Automaton automaton = galAutomata.get("EnemySpawner");

		if (automaton == null) {
			System.err.println("Automate EnemySpawner introuvable");
			return;
		}

		FruityFrankMap.Cell sp = map.enemySpawns().get(0);
		EnemySpawnerEntity spawner = new EnemySpawnerEntity(game, sp.col(), sp.row());

		spawner.setStunt(new BasicStunt(spawner));

		GALBot bot = new GALBot(spawner, automaton, 0.0, new FrankCategories());

		final int[] spawnIndex = { 0 };

		Enemy.EnemyType[] spawnTypes = { Enemy.EnemyType.RED, Enemy.EnemyType.GREEN, Enemy.EnemyType.PURPLE,
				Enemy.EnemyType.ORANGE };

		bot.setFactory((category, at, creator) -> {
			Enemy.EnemyType type = spawnTypes[spawnIndex[0] % spawnTypes.length];
			spawnIndex[0]++;

			Enemy enemy = new Enemy("Enemy_SPAWNED_" + type + "@" + at.x() + "," + at.y(), game, at.x(), at.y(), type);
			enemy.setStunt(new BasicStunt(enemy));

			Automaton enemyAutomaton;
			if (model.state().fruitsRemaining() > model.state().totalFruit() / 2) {
				enemyAutomaton = switch (type) {
				case PURPLE -> galAutomata.get("Tracker");
				case RED -> galAutomata.get("Square");
				case ORANGE -> galAutomata.get("WallFollower");
				case GREEN -> galAutomata.get("EnemyRandom");
				default -> throw new IllegalArgumentException();
				};
			} else {
				enemyAutomaton = switch (type) {
				case PURPLE -> galAutomata.get("Tracker");
				case RED -> galAutomata.get("Tracker");
				case ORANGE -> galAutomata.get("Tracker");
				case GREEN -> galAutomata.get("Tracker");
				default -> throw new IllegalArgumentException();
				};
			}

			if (enemyAutomaton != null) {
				GALBot enemyBot = new GALBot(enemy, enemyAutomaton, enemySpeed, new FrankCategories());
				enemy.setBot(enemyBot);
			} else {
				System.err.println("Automate introuvable pour ennemi spawn : " + type);
			}

			return enemy;
		});

		spawner.setBot(bot);
		model.add(spawner);
	}

	private static class Spawner implements Ticker.TickListener {
		private final FruityFrankModel model;
		private final FruityFrank game;
		private final FruityFrankMap map;
		private final Controller controller;
		private final Camera camera;

		Spawner(FruityFrankModel m, FruityFrank g, FruityFrankMap mp, Controller c, Camera cam) {
			model = m;
			game = g;
			map = mp;
			controller = c;
			camera = cam;
		}

		@Override
		public void onTick(double dt) {
			GameState st = model.state();
			if (!st.isGameOver() && st.frankDied()) {
				st.clearDeath();

				FrankBot bot = spawnFrank(model, game, map, true);
				controller.setPlayerBot(bot);

				Frank frank = model.player();
				if (frank != null && camera != null) {
					camera.smoothCenterOn(frank.center().x(), frank.center().y());
				}
			}
		}
	}

	private static class RenderLoop implements Runnable {
		private final Canvas canvas;

		RenderLoop(Canvas c) {
			canvas = c;
		}

		@Override
		public void run() {
			if (!running)
				return;
			canvas.repaint();
			Runtime.post(this, RENDER_MS);
		}
	}

	private static Map<String, Automaton> loadGalAutomata() {
		Map<String, Automaton> automata = new LinkedHashMap<>();

		String[] files = { "gal_fsm/Tracking.gal", "gal_fsm/Flee.gal", "gal_fsm/EnemyRandom.gal",
				"gal_fsm/AppleFall.gal", "gal_fsm/EnemySpawner.gal", "gal_fsm/Digger.gal", "gal_fsm/Square.gal",
				"gal_fsm/Pickup.gal" };

		for (String file : files) {
			try {
				automata.putAll(GalCompiler.fromFile(file));
			} catch (Exception e) {
				System.err.println("GAL ignoré: " + file + " -> " + e.getMessage());
			}
		}

		return automata;
	}
}
