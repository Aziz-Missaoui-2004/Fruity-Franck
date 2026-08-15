package engine.geometry;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import engine.core.Game;
import engine.entities.Entity;

// = GRID =

public class Grid {

	// FIELDS

	private ISU isu;
	private Axis xAxis, yAxis;

	private int width_ncell, height_ncell;

	private Cell[][] grid;
	private double cmPerCell;

	// CONSTRUCTOR

	public Grid(Game game) {
		width_ncell = game.getWidth_ncell();
		height_ncell = game.getHeight_ncell();
		xAxis = new Axis(game.torusOnX(), (double) width_ncell);
		yAxis = new Axis(game.torusOnY(), (double) height_ncell);
		cmPerCell = game.getCmPerCell();

		isu = game.getIsu();

		grid = new Cell[width_ncell][height_ncell];
		init();
	}

	// INIT

	void init() {
		for (int x = 0; x < width_ncell; x++) {
			for (int y = 0; y < height_ncell; y++)
				grid[x][y] = new Cell(new Position(x, y));
		}
	}

	// GETTER

	public int width() {
		return width_ncell;
	}

	public int height() {
		return height_ncell;
	}

	public Grid.Cell cellAt(Grid.Position p) {
		return grid[p.x_ncell][p.y_ncell];
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.println("Grid " + width_ncell + " x " + height_ncell);
		for (int x = 0; x < width_ncell; x++) {
			for (int y = 0; y < height_ncell; y++) {
				grid[x][y].show(ps);
				ps.print(" ");
			}
			ps.println();
		}
	}

	// == DIMENSION (nb cell) ==

	public class Dimension {
		private int x_ncell, y_ncell;

		// CONSTRUCTOR

		public Dimension(int x_ncell, int y_ncell) {
			assert x_ncell <= width() : "An entity has to fit on the grid (width)";
			assert y_ncell <= height() : "An entity has to fit on the grid (height)";
			assert x_ncell > 0 : "width should be positive";
			assert y_ncell > 0 : "height should be positive";

			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
		}

		// GETTER

		public int x() {
			return x_ncell;
		}

		public int y() {
			return y_ncell;
		}

		// GEOMETRY

		void normalize() {
			x_ncell = xAxis.normalize(x_ncell);
			y_ncell = yAxis.normalize(y_ncell);
		}

		// EQUALS / EQUIV
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Dimension))
				return false;
			Dimension d = (Dimension) o;
			return x_ncell == d.x_ncell && y_ncell == d.y_ncell;
		}

		@Override
		public int hashCode() {
			return 31 * x_ncell + y_ncell;
		}

		public boolean equiv(Dimension d) {
			return this == d || (d.x_ncell == x_ncell && d.y_ncell == y_ncell);
		}

		// CONVERSION

		public ISU.Dimension toISUDimension() {
			return isu.new Dimension(x_ncell * cmPerCell, y_ncell * cmPerCell);
		}

		// SHOW

		void show(PrintStream ps) {
			ps.printf("Grid.Dimension[%d x %d cells]", x_ncell, y_ncell);
		}

	}

	// == VECTOR ==

	public class Vector {

		private int x_ncell;
		private int y_ncell;

		// CONSTRUCTOR

		public Vector(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
		}

		// OPERATION

		public void add(Vector v) {
			int new_x = v.x() + x_ncell;
			int new_y = v.y() + y_ncell;

			x_ncell = new_x;
			y_ncell = new_y;
		}

		// SHOW

		void show(PrintStream ps) {
			ps.printf("Grid.Vector[%d, %d]", x_ncell, y_ncell);
		}

		public int x() {
			return x_ncell;
		}

		public int y() {
			return y_ncell;
		}

	}

	// == POINT ==

	public class Position {

		// CONSTRUCTOR

		private int y_ncell;
		private int x_ncell;

		public Position(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
			normalize();
		}

		void normalize() {
			x_ncell = xAxis.normalize(x_ncell);
			y_ncell = yAxis.normalize(y_ncell);
		}

		// COPY ? if needed

		public Grid.Position copy() {
			return new Position(x_ncell, y_ncell);
		}

		// EQUALS
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Position))
				return false;
			Position p = (Position) o;
			return x_ncell == p.x_ncell && y_ncell == p.y_ncell;
		}

		@Override
		public int hashCode() {
			return 31 * x_ncell + y_ncell;
		}

		// TRANSLATION

		public void translate(Vector v) {
			x_ncell += v.x();
			y_ncell += v.y();
			normalize();
		}

		public void moveNorth(int n_ncell) {
			y_ncell -= n_ncell;
			normalize();
		}

		// ROTATION ? if needed

		public void rotateAround(Grid.Position position, int angle_degree) {
			ISU.Coord centerCoord = toISUCoordCentered();
			ISU.Coord pivotCoord = position.toISUCoordCentered();

			centerCoord.rotateAround(pivotCoord, angle_degree);

			x_ncell = (int) Math.floor(centerCoord.x() / cmPerCell);
			y_ncell = (int) Math.floor(centerCoord.y() / cmPerCell);

			normalize();
		}

		// DISTANCE

		public double distanceTo(Position p) {
			double dx2 = Math.pow(xAxis.distance(p.x_ncell, x_ncell), 2);
			double dy2 = Math.pow(yAxis.distance(p.y_ncell, y_ncell), 2);
			return Math.sqrt(dx2 + dy2);
		}

		// CONVERSION

		/**
		 * @implNote return the coordinates in cm of the top left of corner of this cell
		 */
		public ISU.Coord toISUCoord() {
			double cm = isu.getCmPerCell();
			return isu.new Coord(x_ncell * cm, y_ncell * cm);
		}

		/**
		 * @implNote return the coordinate in cm of the center of corner of this cell
		 */
		public ISU.Coord toISUCoordCentered() {
			double cm = isu.getCmPerCell();
			double half = cm / 2.0;
			return isu.new Coord(x_ncell * cm + half, y_ncell * cm + half);
		}

//		Picture.Pixel toPicturePixel() {
//			return null;
//		}

		// SHOW

		public void show(PrintStream ps) {
			ps.printf("Position : (%d, %d)", x_ncell, y_ncell);
		}

		public int x() {
			return x_ncell;
		}

		public int y() {
			return y_ncell;
		}

	}

	// === CELL ===

	public class Cell {

		Grid.Dimension size;
		Grid.Position position;
		Set<Entity> entities;

		// CONSTRUCTOR

		Cell(Position p) {
			position = p;
			entities = new HashSet<>();
			size = new Dimension(1, 1);
		}

		// ADD

		public void add(Entity e) {
			entities.add(e);
		}

		// REMOVE

		public void remove(Entity e) {
			entities.remove(e);
		}

		// GETTER

		public Grid.Position position() {
			return position;
		}

		public boolean contains(Entity e) {
			return entities.contains(e);
		}

		/** @return a read-only view of the entities currently occupying this cell. */
		public Set<Entity> entities() {
			return Collections.unmodifiableSet(entities);
		}

		// EQUALS

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Cell))
				return false;
			Cell c = (Cell) o;
			return position.equals(c.position);
		}

		@Override
		public int hashCode() {
			return 31 * position.x() + position.y();
		}

		// SHOW

		public void show(PrintStream ps) {
			position.show(ps);
			ps.print("[");
			if (entities.isEmpty()) {
				ps.print(" ");
			} else {
				ps.print(entities.size());
			}
			ps.print("]");
		}

	}
}
