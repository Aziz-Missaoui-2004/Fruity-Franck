package engine.geometry;

import java.io.PrintStream;

import engine.core.Game;

// = ISU =

public class ISU {

	// FIELDS

	private final Axis xAxis;
	private final Axis yAxis;
	private Grid grid;
	private double cmPerCell;
	private double width_cm;
	private double height_cm;
	public static final double epsilon = 1e-9;

	// CONSTRUCTOR

	public ISU(Game game) {
		cmPerCell = game.getCmPerCell();
		width_cm = game.getWidth_cm();
		height_cm = game.getHeight_cm();
		xAxis = new Axis(game.torusOnX(), game.getWidth_cm());
		yAxis = new Axis(game.torusOnY(), game.getHeight_cm());
	}

	// SETTER

	public void set(Grid grid) {
		this.grid = grid;
	}

	// == DIMENSION (cm) ==

	/**
	 * @return the cmPerCell
	 */
	public double getCmPerCell() {
		return cmPerCell;
	}

	public class Dimension {
		private double x_cm, y_cm;

		// CONSTRUCTOR

		public Dimension(double x_cm, double y_cm) {
			assert x_cm > 0 : "width should be positive";
			assert y_cm > 0 : "height should be positive";
			assert x_cm <= width_cm : "An entity has to fit on the grid (width)";
			assert y_cm <= height_cm : "An entity has to fit on the grid (height)";

			this.x_cm = x_cm;
			this.y_cm = y_cm;
		}

		// GEOMETRY

		void normalize() {
			x_cm = xAxis.normalize(x_cm);
			y_cm = yAxis.normalize(y_cm);
		}

		// SETTER

		public void setxy(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;
			normalize();
		}

		// GETTER

		ISU isu() {
			return ISU.this;
		}

		// EQUALS / EQUIV

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Dimension))
				return false;
			Dimension d = (Dimension) o;

			return Double.compare(d.x_cm, x_cm) == 0 && Double.compare(d.y_cm, y_cm) == 0;
		}

		@Override
		public int hashCode() {
			return 31 * Double.hashCode(x_cm) + Double.hashCode(y_cm);
		}

		public boolean equiv(Dimension d) {
			return this == d || Math.abs(d.x_cm - x_cm) < epsilon && Math.abs(d.y_cm - y_cm) < epsilon;
		}

		// GETTER

		public double x() {
			return x_cm;
		}

		public double y() {
			return y_cm;
		}

		// FACTORY

		public ISU.Vector mkScaledVector(double factor) {
			return new Vector(x_cm * factor, y_cm * factor);
		}

		ISU.Vector mkScaledVector(double xFactor, double yFactor) {
			return new Vector(x_cm * xFactor, y_cm * yFactor);
		}

		public ISU.Vector mkVector() {
			return new Vector(x_cm, y_cm);
		}

		// SHOW

		public void show(PrintStream ps) {
			ps.printf("ISU.Dimension[%.2f x %.2f cm]", x_cm, y_cm);
		}

	}

	// == POINT ==

	public class Coord {

		private double x_cm;
		private double y_cm;

		// CONSTRUCTOR

		public Coord(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;
			normalize();
		}

		void normalize() {
			x_cm = xAxis.normalize(x_cm);
			y_cm = yAxis.normalize(y_cm);
		}

		// SHOW

		public void show(PrintStream ps) {
			ps.printf("ISU.Coord[%.2f, %.2f cm]", x_cm, y_cm);
		}

		// EQUALS
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Coord))
				return false;
			Coord c = (Coord) o;
			return Double.compare(c.x_cm, x_cm) == 0 && Double.compare(c.y_cm, y_cm) == 0;
		}

		@Override
		public int hashCode() {
			return 31 * Double.hashCode(x_cm) + Double.hashCode(y_cm);
		}

		// FACTORY

		public ISU.Vector mkVectorToward(Coord target) {
			return new Vector(target.x_cm - x_cm, target.y_cm - y_cm);
		}

		public ISU.Vector nearestDeltaTo(Coord target) {
			return new Vector(xAxis.signedDistance(x_cm, target.x_cm), yAxis.signedDistance(y_cm, target.y_cm));
		}

		// CONVERSION

		public Grid.Position toGridPosition() {
			int cellX = (int) Math.floor(x_cm / getCmPerCell());
			int cellY = (int) Math.floor(y_cm / getCmPerCell());
			return grid.new Position(cellX, cellY);
		}

		// TRANSLATION

		public void translate(ISU.Vector v) {
			x_cm += v.x_cm;
			y_cm += v.y_cm;
			normalize();
		}

		public ISU.Coord mkTranslated(ISU.Vector v) {
			return new Coord(x_cm + v.x_cm, y_cm + v.y_cm);
		}

		// COPY

		public ISU.Coord mkCopy() {
			return new Coord(x_cm, y_cm);
		}

		// ROTATION

		/**
		 * @apiNote rotation around the origin (0,0)
		 * @param angle_degree integer degrees only
		 * 
		 */
		public void rotation(int angle_degree) {
			double rad = Math.toRadians(angle_degree);
			double cos = Math.cos(rad);
			double sin = Math.sin(rad);
			double oldX = x_cm;
			double oldY = y_cm;
			x_cm = oldX * cos - oldY * sin;
			y_cm = oldX * sin + oldY * cos;
			normalize();
		}

		/**
		 * @apiNote rotation around the given center
		 * @param center
		 * @param angle_degree integer degrees only
		 */
		public void rotateAround(Coord center, int angle_degree) {
			double dx = x_cm - center.x_cm;
			double dy = y_cm - center.y_cm;
			double rad = Math.toRadians(angle_degree);
			double cos = Math.cos(rad);
			double sin = Math.sin(rad);
			double newDx = dx * cos - dy * sin;
			double newDy = dx * sin + dy * cos;
			x_cm = center.x_cm + newDx;
			y_cm = center.y_cm + newDy;
			normalize();
		}

		// DISTANCE

		public double distanceTo(Coord pt) {
			double dx = xAxis.distance(x_cm, pt.x_cm);
			double dy = yAxis.distance(y_cm, pt.y_cm);
			return Math.sqrt(dx * dx + dy * dy);
		}

		public double x() {
			return x_cm;
		}

		public double y() {
			return y_cm;
		}

		public ISU getISU() {
			return ISU.this;
		}

	}

	// == VECTOR ==

	/**
	 * @apiNote The Vector class defines canonical vectors with origin in (0,0)
	 *          poiting at a target coordinate.
	 * @apiNote Canonocal vectors are defined by their target Coord.
	 */
	public class Vector {

		private double y_cm;
		private double x_cm;

		// CONSTRUCTOR

		public Vector(double targetX_cm, double targetY_cm) {
			this.x_cm = targetX_cm;
			this.y_cm = targetY_cm;
		}

		// OPERATOR

		void add(Vector v) {
			x_cm += v.x_cm;
			y_cm += v.y_cm;
		}

		void scale(double factor) {
			x_cm *= factor;
			y_cm *= factor;
		}

		void scale(double xFactor, double yFactor) {
			x_cm *= xFactor;
			y_cm *= yFactor;
		}

		/**
		 * @apiNote produit scalaire
		 * @param v
		 * @return le produit scalaire de `this` et du vecteur v
		 */
		public double dot(ISU.Vector v) {
			return x_cm * v.x_cm + y_cm * v.y_cm;
		}

		public double norm() {
			return Math.sqrt(x_cm * x_cm + y_cm * y_cm);
		}

		/**
		 * @apiNote rend le vecteur unitaire, ie. de norme = 1
		 */
		void unity() {
			double n = norm();
			if (n != 0) {
				x_cm /= n;
				y_cm /= n;
			}
		}

		// TURN

		/**
		 * @apiNote turn the vector itself
		 * @implNote the center of the rotation is the origin of the vector
		 * @param angle_degree
		 */
		void turn(int angle_degree) {
			double rad = Math.toRadians(angle_degree);
			double cos = Math.cos(rad);
			double sin = Math.sin(rad);
			double newX = x_cm * cos - y_cm * sin;
			double newY = x_cm * sin + y_cm * cos;
			x_cm = newX;
			y_cm = newY;
		}

		public double x() {
			return x_cm;
		}

		public double y() {
			return y_cm;
		}

	}

	public boolean isTorusX() {
		return xAxis.onTorus;
	}

	public boolean isTorusY() {
		return yAxis.onTorus;
	}

}
