package game.graphics;

import engine.entities.Entity;
import engine.geometry.ISU;
import engine.graphics.Avatar;
import engine.graphics.View;

import game.Category;

import game.entities.Gum;

import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

// == PACMAN AVATAR ==

/**
 * Draws one game {@link Entity} from the {@link Sprites} sheet, scaled to its
 * cell. Pac is a single left-facing chomp sprite rotated to its heading (Pac is
 * radially symmetric apart from the mouth, exactly as the original game does
 * it); the ghost has real directional frames and swaps to the frightened sprite
 * when its {@link #mode()} is {@link #FRIGHTENED}; gums are small filled dots
 * (pellets larger). The only game knowledge is the entity's {@link Category}.
 */
public class PacManAvatar extends Avatar {

	/** Ghost appearance mode set by the game while a power pellet is active. */
	public static final String FRIGHTENED = "frightened";

	// Heading codes.
	private static final int E = 0, N = 1, W = 2, S = 3;

	private static final long ANIM_PERIOD_NS = 110_000_000L; // frame step
	private static final int[] CHOMP = { 2, 1, 0, 1 }; // closed→half→open→half

	private final int pixelPerCm;
	private final int cellPx;
	private final Category category;
	private final Sprites sprites;

	private int facing; // last non-zero heading

	public PacManAvatar(Entity entity, View view, int zOrder, int pixelPerCm, int cellPx, Category category,
			Sprites sprites) {
		super(entity, view, zOrder);
		this.pixelPerCm = pixelPerCm;
		this.cellPx = cellPx;
		this.category = category;
		this.sprites = sprites;
		this.facing = (category == Category.PAC) ? W : E; // Pac idles facing left
	}

	@Override
	public void paint(Graphics g) {
		updateFacing();
		int[] s = screen();
		int cx = s[0];
		int cy = s[1];
		switch (category) {
		case PAC -> paintPac(g, cx, cy);
		case GHOST -> paintGhost(g, cx, cy);
		default -> paintDot(g, cx, cy); // gums / pellets
		}
	}

	// PAC: rotate the left-facing chomp sprite to the heading.

	private void paintPac(Graphics g, int cx, int cy) {
		sprites.ensureLoaded(g);
		BufferedImage img = sprites.pac(CHOMP[animFrame(CHOMP.length)]);
		int half = cellPx / 2;
		Object saved = g.getTransform();
		g.translate(cx, cy);
		g.rotate(rotationFor(facing)); // source faces LEFT (W)
		g.drawImage(img, -half, -half, cellPx, cellPx);
		g.setTransform(saved);
	}

	/**
	 * Radians to turn the LEFT-facing source so its mouth points at {@code facing}.
	 */
	private static double rotationFor(int facing) {
		return switch (facing) {
		case W -> Math.PI;
		case N -> -Math.PI / 2;
		case E -> 0.0;
		case S -> Math.PI / 2;
		default -> 0.0;
		};
	}

	// GHOST: directional frames, or frightened blue when its mode says so.

	private void paintGhost(Graphics g, int cx, int cy) {
		sprites.ensureLoaded(g);
		int frame = animFrame(2);
		BufferedImage img = FRIGHTENED.equals(mode()) ? sprites.fright(frame) : sprites.ghost(ghostBase(facing), frame);
		int half = cellPx / 2;
		g.drawImage(img, cx - half, cy - half, cellPx, cellPx);
	}

	private static int ghostBase(int facing) {
		return switch (facing) {
		case E -> Sprites.GHOST_RIGHT;
		case W -> Sprites.GHOST_LEFT;
		case N -> Sprites.GHOST_UP;
		case S -> Sprites.GHOST_DOWN;
		default -> Sprites.GHOST_RIGHT;
		};
	}

	// GUM / PELLET: a filled dot sized from the entity's bounding.

	private void paintDot(Graphics g, int cx, int cy) {
		boolean power = entity instanceof Gum gum && gum.isPower();
		int r = (int) Math.round(cellPx * (power ? 0.25 : 0.1)); // pellet vs gum radius
		int[] rgb = power ? new int[] { 255, 255, 255 } : Palette.rgb(category);
		g.setColor(g.getColor(255, rgb[0], rgb[1], rgb[2]));
		g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
	}

	// HELPERS

	private void updateFacing() {
		ISU.Vector v = entity.getLinearSpeed();
		if (v == null || (v.x() == 0 && v.y() == 0))
			return;
		if (Math.abs(v.x()) >= Math.abs(v.y()))
			facing = v.x() >= 0 ? E : W;
		else
			facing = v.y() < 0 ? N : S;
	}

	private int animFrame(int n) {
		return (int) ((System.nanoTime() / ANIM_PERIOD_NS) % n);
	}

	private int px(double cm) {
		return (int) Math.round(cm * pixelPerCm);
	}

	/** Screen pixel of the entity's centre, via the view's camera if it has one. */
	private int[] screen() {
		if (view.camera() != null)
			return view.camera().worldToScreen(entity.center().x(), entity.center().y());
		return new int[] { px(entity.center().x()), px(entity.center().y()) };
	}
}
