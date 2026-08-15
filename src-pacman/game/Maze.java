package game;

import java.util.ArrayList;
import java.util.List;

// == MAZE ==

/**
 * The demo arena — deliberately <b>larger than the viewport</b> so the engine's
 * scrolling {@link engine.graphics.Camera camera} and
 * {@link engine.graphics.Minimap minimap} are exercised. A torus (no border)
 * tiled with a regular lattice of solid blocks separated by lanes, with gums on
 * the open cells, power pellets in the corners, and far-apart spawns so the GAL
 * ghost has to chase across the world.
 *
 * <p>
 * Walls are world geometry, not entities: consulted by
 * {@link PacManModel#canOccupy} and drawn by the painter; {@link #isWall} wraps
 * coordinates for the torus.
 */
public final class Maze {

	/** A grid cell (column, row). */
	public record Cell(int col, int row) {
	}

	private final int cols;
	private final int rows;
	private final boolean[][] wall; // [col][row]

	private final List<Cell> gums = new ArrayList<>();
	private final List<Cell> pellets = new ArrayList<>();
	private final Cell pacSpawn;
	private final Cell ghostSpawn;

	/** Build the fixed demo arena (31×31, bigger than the 19×19 viewport). */
	public Maze() {
		this.cols = 31;
		this.rows = 31;
		this.wall = new boolean[cols][rows];

		// Regular lattice of 3×3 solid blocks with 3-cell lanes between them.
		for (int c0 = 3; c0 + 2 < cols; c0 += 6)
			for (int r0 = 3; r0 + 2 < rows; r0 += 6)
				block(c0, r0, c0 + 2, r0 + 2);

		this.pacSpawn = new Cell(1, 1);
		this.ghostSpawn = new Cell(cols - 2, rows - 2); // far corner: a long chase

		// Power pellets in open lane corners (not on a spawn).
		pellets.add(new Cell(cols - 2, 1));
		pellets.add(new Cell(1, rows - 2));
		pellets.add(new Cell(cols / 2, 1));
		pellets.add(new Cell(1, rows / 2));

		// A gum on every open cell that is not a spawn or a pellet.
		for (int c = 0; c < cols; c++)
			for (int r = 0; r < rows; r++) {
				Cell cell = new Cell(c, r);
				if (!wall[c][r] && !cell.equals(pacSpawn) && !cell.equals(ghostSpawn) && !pellets.contains(cell))
					gums.add(cell);
			}
	}

	private void block(int c0, int r0, int c1, int r1) {
		for (int c = c0; c <= c1; c++)
			for (int r = r0; r <= r1; r++)
				wall[c][r] = true;
	}

	/** @return whether the cell (torus-wrapped) is a wall. */
	public boolean isWall(int col, int row) {
		int c = Math.floorMod(col, cols);
		int r = Math.floorMod(row, rows);
		return wall[c][r];
	}

	public int cols() {
		return cols;
	}

	public int rows() {
		return rows;
	}

	public List<Cell> gums() {
		return gums;
	}

	public List<Cell> pellets() {
		return pellets;
	}

	public Cell pacSpawn() {
		return pacSpawn;
	}

	public Cell ghostSpawn() {
		return ghostSpawn;
	}
}
