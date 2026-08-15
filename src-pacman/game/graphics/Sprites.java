package game.graphics;

import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

// == SPRITES ==

public final class Sprites {

	public static final String SHEET = "sprites/pacman_spritesheet.png";

	private static final int X0 = 457; // x of the first sprite's content
	private static final int Y0 = 1; // y of row 0
	private static final int PITCH = 16;
	private static final int SIZE = 14; // content size grabbed per cell

	// Pac chomp, facing LEFT: index 0 = wide open, 1 = half, 2 = closed circle.
	private final BufferedImage[] pac = new BufferedImage[3];
	// Red ghost, 2 frames per direction: [R0,R1, L0,L1, U0,U1, D0,D1].
	private final BufferedImage[] ghost = new BufferedImage[8];
	// Frightened: [blue0, blue1, white0, white1].
	private final BufferedImage[] fright = new BufferedImage[4];

	private boolean loaded = false;

	/** Direction base index into the ghost array. */
	public static final int GHOST_RIGHT = 0, GHOST_LEFT = 2, GHOST_UP = 4, GHOST_DOWN = 6;

	/** Decode and slice the sheet on first paint; no-op afterwards. */
	public void ensureLoaded(Graphics g) {
		if (loaded)
			return;
		BufferedImage sheet = g.load(SHEET);
		for (int i = 0; i < 3; i++)
			pac[i] = cell(sheet, i, 0);
		for (int i = 0; i < 8; i++)
			ghost[i] = cell(sheet, i, 4);
		for (int i = 0; i < 4; i++)
			fright[i] = cell(sheet, 8 + i, 4);
		loaded = true;
	}

	public boolean isLoaded() {
		return loaded;
	}

	private BufferedImage cell(BufferedImage sheet, int col, int row) {
		return sheet.getSubimage(X0 + col * PITCH, Y0 + row * PITCH, SIZE, SIZE);
	}

	/** A Pac chomp frame (0 = open, 1 = half, 2 = closed), drawn facing LEFT. */
	public BufferedImage pac(int chompFrame) {
		return pac[chompFrame];
	}

	/**
	 * A directional red-ghost frame: {@code base} ∈ {GHOST_RIGHT…}, {@code frame} ∈
	 * {0,1}.
	 */
	public BufferedImage ghost(int base, int frame) {
		return ghost[base + frame];
	}

	/** A frightened-ghost frame (0,1 = blue; 2,3 = white flash). */
	public BufferedImage fright(int frame) {
		return fright[frame];
	}
}
