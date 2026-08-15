package engine.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import engine.geometry.Grid;
import engine.geometry.ISU;
import engine.shapes.Circle;
import engine.shapes.Rect;
import engine.shapes.iShape;

public class Hitbox {

	private final Entity owner;
	private final Grid grid;
	private final boolean torusX, torusY;
	private final double cmPerCell;

	// world AABB (cm), in a frame continuous with the entity centre (see rebuild)
	private double aabbMinX, aabbMaxX, aabbMinY, aabbMaxY;

	// occupancy + cached integer footprint used to skip redeploys when unchanged
	private final Set<Grid.Cell> occupied = new HashSet<>();
	private boolean hasFootprint = false;
	private int cellMinX, cellMaxX, cellMinY, cellMaxY;

	/**
	 * @param owner the entity this hitbox represents (registered into the cells it
	 *              occupies)
	 * @param isu   the ISU coordinate system (with torus settings)
	 * @param grid  the grid for cell mapping
	 */
	public Hitbox(Entity owner, ISU isu, Grid grid) {
		this.owner = owner;
		this.grid = grid;
		this.torusX = isu.isTorusX();
		this.torusY = isu.isTorusY();
		this.cmPerCell = isu.getCmPerCell();
		resetAabb();
	}

	private void resetAabb() {
		aabbMinX = aabbMinY = Double.POSITIVE_INFINITY;
		aabbMaxX = aabbMaxY = Double.NEGATIVE_INFINITY;
	}

	// --------------------------------------------------------------------
	// Refresh
	// --------------------------------------------------------------------

	public void rebuild(Set<iShape> shapes, ISU.Coord center) {
		resetAabb();
		for (iShape shape : shapes) {
			if (shape instanceof Circle) {
				Circle c = (Circle) shape;
				double r = c.getRadius();
				ISU.Vector d = center.nearestDeltaTo(c.getCenter());
				double cx = center.x() + d.x();
				double cy = center.y() + d.y();
				accumulate(cx - r, cy - r);
				accumulate(cx + r, cy + r);
			} else if (shape instanceof Rect) {
				Rect r = (Rect) shape;
				ISU.Vector d = center.nearestDeltaTo(r.getCenter());
				double cx = center.x() + d.x();
				double cy = center.y() + d.y();
				for (double[] corner : r.cornersAt(cx, cy))
					accumulate(corner[0], corner[1]);
			}
		}
		sync(false);
	}

	private void accumulate(double x, double y) {
		aabbMinX = Math.min(aabbMinX, x);
		aabbMaxX = Math.max(aabbMaxX, x);
		aabbMinY = Math.min(aabbMinY, y);
		aabbMaxY = Math.max(aabbMaxY, y);
	}

	// --------------------------------------------------------------------
	// Occupancy
	// --------------------------------------------------------------------

	/**
	 * Force the grid occupancy to match the current AABB, ignoring the
	 * footprint-unchanged shortcut. Safe to call repeatedly (idempotent).
	 */
	public void deploy() {
		sync(true);
	}

	/** Release every grid cell this hitbox occupies. */
	public void retract() {
		releaseCells();
		hasFootprint = false;
	}

	private void releaseCells() {
		for (Grid.Cell cell : occupied)
			cell.remove(owner);
		occupied.clear();
	}

	/**
	 * Map the current AABB to an integer cell range and, if that range differs from
	 * the last one (or {@code force}), re-occupy the grid accordingly.
	 */
	private void sync(boolean force) {
		if (getAabbMinX() > aabbMaxX || aabbMinY > aabbMaxY) {
			retract();
			return;
		}
		double eps = ISU.epsilon;
		int nMinX = (int) Math.floor((getAabbMinX() + eps) / cmPerCell);
		int nMaxX = (int) Math.floor((aabbMaxX - eps) / cmPerCell);
		int nMinY = (int) Math.floor((aabbMinY + eps) / cmPerCell);
		int nMaxY = (int) Math.floor((aabbMaxY - eps) / cmPerCell);

		if (nMaxX < nMinX || nMaxY < nMinY) {
			retract();
			return;
		}

		if (!force && hasFootprint && nMinX == cellMinX && nMaxX == cellMaxX && nMinY == cellMinY && nMaxY == cellMaxY)
			return; // footprint unchanged — the grid already reflects it

		cellMinX = nMinX;
		cellMaxX = nMaxX;
		cellMinY = nMinY;
		cellMaxY = nMaxY;
		hasFootprint = true;
		occupyRange();
	}

	private void occupyRange() {
		releaseCells();
		for (int[] xRange : splitIntervalOnTorus(cellMinX, cellMaxX, grid.width(), torusX)) {
			for (int[] yRange : splitIntervalOnTorus(cellMinY, cellMaxY, grid.height(), torusY)) {
				for (int x = xRange[0]; x <= xRange[1]; x++)
					for (int y = yRange[0]; y <= yRange[1]; y++) {
						Grid.Cell cell = grid.cellAt(grid.new Position(x, y));
						cell.add(owner);
						occupied.add(cell);
					}
			}
		}
	}

	// --------------------------------------------------------------------
	// Broad phase
	// --------------------------------------------------------------------

	/**
	 * Broad phase: do this hitbox and {@code other} occupy overlapping (or
	 * neighbouring) cells? Conservative — it dilates by one cell so a true shape
	 * overlap is never missed at a cell boundary, and the narrow phase
	 * ({@code Bounding.intersects}) then confirms or rejects the candidate.
	 */
	public boolean sharesCellsWith(Hitbox other) {
		Set<Grid.Cell> s1 = occupied;
		Set<Grid.Cell> s2 = other.occupied;
		if (s1.isEmpty() || s2.isEmpty())
			return false;
		// iterate the smaller set
		if (s1.size() > s2.size()) {
			Set<Grid.Cell> tmp = s1;
			s1 = s2;
			s2 = tmp;
		}
		for (Grid.Cell c : s1) {
			Grid.Position p = c.position();
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					Grid.Position np = grid.new Position(p.x() + dx, p.y() + dy);
					if (s2.contains(grid.cellAt(np)))
						return true;
				}
			}
		}
		return false;
	}

	// --------------------------------------------------------------------
	// Getters
	// --------------------------------------------------------------------

	/** @return the cells this hitbox currently occupies (read-only view intent). */
	public Set<Grid.Cell> occupiedCells() {
		return occupied;
	}

	/**
	 * World AABB extents (cm), in the centre-continuous frame of the last rebuild.
	 */
	public double getMinX() {
		return getAabbMinX();
	}

	public double getMaxX() {
		return aabbMaxX;
	}

	public double getMinY() {
		return aabbMinY;
	}

	public double getMaxY() {
		return aabbMaxY;
	}

	// --------------------------------------------------------------------
	// Helpers
	// --------------------------------------------------------------------

	private List<int[]> splitIntervalOnTorus(int min, int max, int size, boolean isTorus) {
		List<int[]> intervals = new ArrayList<>();
		if (!isTorus) {
			int clampedMin = Math.max(0, min);
			int clampedMax = Math.min(size - 1, max);
			if (clampedMin <= clampedMax) {
				intervals.add(new int[] { clampedMin, clampedMax });
			}
			return intervals;
		}

		// If interval covers whole world, return single interval [0, size-1]
		if (max - min + 1 >= size) {
			intervals.add(new int[] { 0, size - 1 });
			return intervals;
		}

		// Normalize min to [0, size-1]
		int normMin = ((min % size) + size) % size;
		int normMax = normMin + (max - min);
		if (normMax < size) {
			intervals.add(new int[] { normMin, normMax });
		} else {
			intervals.add(new int[] { normMin, size - 1 });
			intervals.add(new int[] { 0, normMax - size });
		}
		return intervals;
	}

	public double getAabbMinX() {
		return aabbMinX;
	}

	public double getAabbMinY() {
		return aabbMinY;
	}

	public double getAabbMaxY() {
		return aabbMaxY;
	}

	public double getAabbMaxX() {
		return aabbMaxX;
	}

}
