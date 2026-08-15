package frank.graphics;

import engine.entities.Entity;
import engine.geometry.ISU;
import engine.graphics.Avatar;
import engine.graphics.View;
import frank.entities.Apple;
import frank.entities.Enemy;
import frank.entities.Frank;
import frank.entities.Fruit;
import frank.entities.GrassHigh;
import frank.game.Category;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class FrankAvatar extends Avatar {

	private static final long ANIM_NS = 150_000_000L;
	private static final int E = 0, W = 1, N = 2, S = 3;

	private final int pixelPerCm, cellPx;
	private final Category category;
	private final FrankSprites sprites;
	private int facing = E;

	public FrankAvatar(Entity entity, View view, int z, int pixelPerCm, int cellPx, Category category,
			FrankSprites sprites) {
		super(entity, view, z);
		this.pixelPerCm = pixelPerCm;
		this.cellPx = cellPx;
		this.category = category;
		this.sprites = sprites;
	}

	@Override
	public void paint(Graphics g) {
		updateFacing();
		int[] s = screen();
		int cx = s[0], cy = s[1];
		switch (category) {
		case GRASS_LOW -> grassLow(g, cx, cy);
		case GRASS_HIGH -> grassHigh(g, cx, cy);
		case FRANK -> drawFrank(g, cx, cy);
		case ENEMY -> drawEnemy(g, cx, cy);
		case ENEMY_SPAWNER -> drawEnemySpawner(g, cx, cy);
		case FRUIT -> drawFruit(g, cx, cy);
		case APPLE -> drawApple(g, cx, cy);
		case CHERRY -> drawCherry(g, cx, cy);
		case CHERRY_PROJECTILE -> dot(g, cx, cy, cellPx / 5, 255, 60, 60);
		default -> {
		}
		}
	}

	// Le petit gazon (1cm) : fine bande au BAS de la cellule.
	// cy est déjà le centre décalé vers le bas de l'entité.
	private void grassLow(Graphics g, int cx, int cy) {
		g.setColor(g.getColor(255, 22, 70, 14));
		int w = (int) (cellPx * 0.95);
		int h = Math.max(3, (int) (cellPx * 0.10));
		g.fillRect(cx - w / 2, cy - h / 2, w, h);
	}

	// Le gros gazon (9cm) : grand bloc au HAUT de la cellule, avec fade.
	private void grassHigh(Graphics g, int cx, int cy) {
		int alpha = 255;
		if (entity instanceof GrassHigh gh && gh.isFading())
			alpha = (int) (255 * (gh.fadeTimer() / (double) GrassHigh.FADE_TICKS));
		int w = (int) (cellPx * 0.95);
		int h = (int) (cellPx * 0.90);
		// Corps du gazon
		g.setColor(g.getColor(alpha, 46, 150, 32));
		g.fillRect(cx - w / 2, cy - h / 2, w, h);
		// Texture : quelques brins plus sombres en haut
		g.setColor(g.getColor(alpha, 30, 105, 20));
		g.fillRect(cx - w / 2, cy - h / 2, w, 3);
	}

	private void drawFrank(Graphics g, int cx, int cy) {
		 if (entity instanceof Frank f && !f.blinkVisible()) {
		        return; }
		BufferedImage img = lazyFrank(g);
		if (img != null)
			drawOnGrass(g, img, cx, cy, cellPx, 20, 15);
		else
			drawFrankShape(g, cx, cy);
	}

	private void drawEnemy(Graphics g, int cx, int cy) {
		BufferedImage img = lazyEnemy(g);
		if (img != null)
			drawOnGrass(g, img, cx, cy, cellPx, 20, 15);
		else
			drawEnemyShape(g, cx, cy);
	}
	
	private void drawEnemySpawner(Graphics g, int cx, int cy) {
	    BufferedImage img = lazyEnemySpawner(g);
	    if (img != null)
	        drawOnGrass(g, img, cx, cy, cellPx, 20, 15);
	    else
	        drawEnemyShape(g, cx, cy);
	}
	/**
	 * Pose le sprite de sorte que ses PIEDS touchent le petit gazon (1cm) qui se
	 * trouve tout en bas de la cellule. Le petit gazon occupe les ~10% du bas, donc
	 * les pieds doivent arriver à ~90% de la hauteur de la cellule depuis le haut.
	 */
	private void drawOnGrass(Graphics g, BufferedImage im, int cx, int cy, int maxSize, int sprW, int sprH) {
		double scale = (double) maxSize / Math.max(sprW, sprH);
		int w = (int) (sprW * scale);
		int h = (int) (sprH * scale);

		// Haut de la cellule = cy - cellPx/2
		// Le petit gazon commence à ~90% de la hauteur depuis le haut
		int cellTop = cy - cellPx / 2;
		int feetY = cellTop + (int) (cellPx * 0.90); // les pieds posés sur le petit gazon

		g.drawImage(im, cx - w / 2, feetY - h, w, h);
	}

	private void drawFruit(Graphics g, int cx, int cy) {
		BufferedImage img = lazyFruit(g);
		if (img != null)
			drawImgRatio(g, img, cx, cy, (int) (cellPx * 0.75), 20, 15);
		else
			dot(g, cx, cy, cellPx / 3, 255, 130, 0);
	}

	private void drawApple(Graphics g, int cx, int cy) {
		BufferedImage img = lazyApple(g);
		if (img != null)
			drawImgRatio(g, img, cx, cy, (int) (cellPx * 0.85), 20, 15);
		else
			drawAppleShape(g, cx, cy);
	}

	private void drawCherry(Graphics g, int cx, int cy) {
		BufferedImage img = lazyCherry(g);
		if (img != null)
			drawImgRatio(g, img, cx, cy, (int) (cellPx * 0.7), 20, 15);
		else
			drawCherryShape(g, cx, cy);
	}

	/**
	 * Dessine une image en respectant son ratio, posée sur le BAS de la cellule
	 * (pieds vers le petit gazon) au lieu d'être centrée verticalement.
	 */
	private void drawImgRatio(Graphics g, BufferedImage im, int cx, int cy, int maxSize, int sprW, int sprH) {
		double scale = (double) maxSize / Math.max(sprW, sprH);
		int w = (int) (sprW * scale);
		int h = (int) (sprH * scale);
		// Aligné par le bas : les pieds posés près du bas de la cellule
		int footY = cy + cellPx / 2; // bas de la cellule
		g.drawImage(im, cx - w / 2, footY - h - 2, w, h); // -2 px pour poser sur le petit gazon
	}
	// ── Formes vectorielles de secours (nettes, pas pixelisées) ──

	private void drawFrankShape(Graphics g, int cx, int cy) {
		int r = (int) (cellPx * 0.4);
		// corps jaune
		g.setColor(g.getColor(255, 255, 205, 40));
		g.fillOval(cx - r, cy - r, r * 2, r * 2);
		// yeux selon direction
		g.setColor(g.getColor(255, 30, 30, 30));
		int ex = (facing == W) ? -r / 2 : r / 2;
		g.fillOval(cx + ex - 2, cy - r / 3, 4, 4);
	}

	private void drawEnemyShape(Graphics g, int cx, int cy) {
		int r = (int) (cellPx * 0.4);
		// corps rouge : cercle plein
		g.setColor(g.getColor(255, 220, 50, 50));
		g.fillOval(cx - r, cy - r, r * 2, r * 2);
		// deux yeux blancs
		g.setColor(g.getColor(255, 255, 255, 255));
		g.fillOval(cx - r / 2 - 2, cy - r / 4, 5, 5);
		g.fillOval(cx + r / 2 - 3, cy - r / 4, 5, 5);
		// deux pupilles noires
		g.setColor(g.getColor(255, 0, 0, 0));
		g.fillOval(cx - r / 2, cy - r / 4 + 1, 3, 3);
		g.fillOval(cx + r / 2 - 1, cy - r / 4 + 1, 3, 3);
	}

	private void drawAppleShape(Graphics g, int cx, int cy) {
		int r = (int) (cellPx * 0.38);
		g.setColor(g.getColor(255, 210, 35, 35));
		g.fillOval(cx - r, cy - r, r * 2, r * 2);
		// tige verte
		g.setColor(g.getColor(255, 90, 160, 40));
		g.fillRect(cx - 1, cy - r - 3, 3, 5);
	}

	private void drawCherryShape(Graphics g, int cx, int cy) {
		int r = (int) (cellPx * 0.22);
		g.setColor(g.getColor(255, 200, 0, 0));
		g.fillOval(cx - r - 3, cy, r * 2, r * 2);
		g.fillOval(cx + 3 - r, cy, r * 2, r * 2);
		g.setColor(g.getColor(255, 90, 160, 40));
		g.fillRect(cx - 1, cy - r, 2, r);
	}

	// ── Helpers ──

	private BufferedImage lazyFrank(Graphics g) {
	    sprites.ensureLoaded(g);

	    ISU.Vector v = entity.getLinearSpeed();
	    boolean moving = v != null && (v.x() != 0 || v.y() != 0);

	    // Important : seulement 2 frames, pas 4.
	    // Sinon  mélanges les frames avec et sans projectile.
	    int animFrame = moving ? frame(2) : 0;

	    boolean hasCherry = entity instanceof Frank f && f.hasCherry();

	    return sprites.frank(facing, animFrame, hasCherry);
	}

	private BufferedImage lazyEnemy(Graphics g) {
		sprites.ensureLoaded(g);
		return entity instanceof Enemy e ? sprites.enemy(e.type(), facing, frameSlow(2)) : null;
	}

	private BufferedImage lazyEnemySpawner(Graphics g) {
	    sprites.ensureLoaded(g);
	    return sprites.enemySpawner();
	}
	
	private int frameSlow(int n) {
		return (int) ((System.nanoTime() / 300_000_000L) % n);
	}

	private BufferedImage lazyFruit(Graphics g) {
		sprites.ensureLoaded(g);
		return entity instanceof Fruit f ? sprites.fruit(f.type()) : null;
	}

	private BufferedImage lazyApple(Graphics g) {
		sprites.ensureLoaded(g);
		return sprites.apple();
	}

	private BufferedImage lazyCherry(Graphics g) {
		sprites.ensureLoaded(g);
		return sprites.cherry();
	}

	private void drawImg(Graphics g, BufferedImage im, int cx, int cy, int size) {
		int h = size / 2;
		g.drawImage(im, cx - h, cy - h, size, size);
	}

	private void dot(Graphics g, int cx, int cy, int rad, int r, int gg, int b) {
		g.setColor(g.getColor(255, r, gg, b));
		g.fillOval(cx - rad, cy - rad, rad * 2, rad * 2);
	}

	private void updateFacing() {
		ISU.Vector v = entity.getLinearSpeed();
		if (v == null || (v.x() == 0 && v.y() == 0))
			return;
		if (Math.abs(v.x()) >= Math.abs(v.y()))
			facing = v.x() >= 0 ? E : W;
		else
			facing = v.y() < 0 ? N : S;
	}

	private int frame(int n) {
		return (int) ((System.nanoTime() / ANIM_NS) % n);
	}

	private int[] screen() {
		double x = entity.center().x();
		double y = entity.center().y();

		if (entity instanceof Apple apple) {
			x += apple.visualOffsetX();
			y += apple.visualOffsetY();
		}

		if (view.camera() != null)
			return view.camera().worldToScreen(x, y);
		return new int[] { (int) Math.round(x * pixelPerCm),
				(int) Math.round(y * pixelPerCm) };
	}
}
