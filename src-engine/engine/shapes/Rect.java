package engine.shapes;

import engine.geometry.ISU;

public class Rect extends Shape {

	private double halfWidth, halfHeight;
	private int angle_degree;

	/**
	 * Corner offsets relative to the centre, already rotated by
	 * {@link #angle_degree} and ordered counter-clockwise from the lower-left local
	 * corner. Computed lazily and cached: it depends only on the (fixed) size and
	 * the orientation, so a purely translating box reuses it every tick instead of
	 * recomputing trig. Invalidated by {@link #rotate(int)}.
	 */
	private double[][] localCorners;
	private boolean cornersDirty = true;

	// CONSTRUCTOR
	public Rect(ISU.Coord center, ISU.Dimension size, int angle_degree) {
		super(center);
		this.halfWidth = size.x() / 2;
		this.halfHeight = size.y() / 2;
		this.angle_degree = Math.floorMod(angle_degree, 360);
	}

	// TRANSLATION
	public void translate(ISU.Vector v) {
		getCenter().translate(v);
	}

	// ROTATION
	public void rotate(int angle_degree) {
		this.angle_degree = Math.floorMod(angle_degree + this.angle_degree, 360);
		cornersDirty = true;
	}

	// CORNERS

	/**
	 * @return the rotated, centre-relative corner offsets (the trig is done once
	 *         per orientation here, then cached). Callers must treat the result as
	 *         read-only — it is the live cache.
	 */
	private double[][] localCorners() {
		if (cornersDirty) {
			double rad = Math.toRadians(angle_degree);
			double cos = Math.cos(rad);
			double sin = Math.sin(rad);
			double[][] local = { { -halfWidth, -halfHeight }, { halfWidth, -halfHeight }, { halfWidth, halfHeight },
					{ -halfWidth, halfHeight } };
			localCorners = new double[4][2];
			for (int i = 0; i < 4; i++) {
				localCorners[i][0] = local[i][0] * cos - local[i][1] * sin;
				localCorners[i][1] = local[i][0] * sin + local[i][1] * cos;
			}
			cornersDirty = false;
		}
		return localCorners;
	}

	/**
	 * @return the four world-space corners of this oriented box, as {x, y} pairs,
	 *         ordered counter-clockwise from the lower-left local corner.
	 */
	public double[][] corners() {
		return cornersAt(center.x(), center.y());
	}

	/**
	 * @implNote Reuses the cached rotated corner offsets and merely translates them
	 *           to {@code (cx, cy)} — no trigonometry unless the orientation
	 *           changed.
	 */
	public double[][] cornersAt(double cx, double cy) {
		double[][] local = localCorners();
		double[][] world = new double[4][2];
		for (int i = 0; i < 4; i++) {
			world[i][0] = cx + local[i][0];
			world[i][1] = cy + local[i][1];
		}
		return world;
	}

	// INTERSECTION

	@Override
	public boolean intersects(iShape shape) {
		return shape.intersects(this);
	}

	// === Rect/Circle Intersection ===
	@Override
	public boolean intersects(Circle circle) {
		return new RectCircleIntersection(this, circle).intersects();
	}

	// Inner class for Rect-Circle
	class RectCircleIntersection {
		private Rect rect;
		private Circle circle;
		private double localX, localY; // circle center in rectangle's local coords

		RectCircleIntersection(Rect rect, Circle circle) {
			this.rect = rect;
			this.circle = circle;
			ISU.Vector delta = rect.getCenter().nearestDeltaTo(circle.getCenter());
			double dx = delta.x();
			double dy = delta.y();
			double rad = Math.toRadians(-rect.angle_degree);
			double cos = Math.cos(rad);
			double sin = Math.sin(rad);
			localX = dx * cos - dy * sin;
			localY = dx * sin + dy * cos;
		}

		private boolean intersects() {
			double closestX = clamp(localX, -rect.halfWidth, rect.halfWidth);
			double closestY = clamp(localY, -rect.halfHeight, rect.halfHeight);
			double dx = closestX - localX;
			double dy = closestY - localY;
			return (dx * dx + dy * dy) <= circle.getRadius() * circle.getRadius();
		}

		private double clamp(double p, double l, double r) {
			if (p < l)
				return l;
			if (p > r)
				return r;
			return p;
		}
	}

	// === Rect/Rect Intersection ===

	/**
	 * @implNote When both boxes are axis-aligned (orientation a multiple of 90°,
	 *           the only case the non-rotating games produce) a plain AABB overlap
	 *           test settles it — the four SAT axes collapse to the two cardinals
	 *           and the projections to min/max of x and y, so the general
	 *           {@link RectRectIntersection} (corner build, edge-normal
	 *           normalisation, 4×8 projections) is skipped. Any oblique box falls
	 *           back to the Separating Axis Theorem.
	 */
	@Override
	public boolean intersects(Rect rect) {
		if (isAxisAligned() && rect.isAxisAligned()) {
			ISU.Vector delta = center.nearestDeltaTo(rect.getCenter()); // torus-aware
			return Math.abs(delta.x()) <= extentX() + rect.extentX()
					&& Math.abs(delta.y()) <= extentY() + rect.extentY();
		}
		return new RectRectIntersection(this, rect).intersects();
	}

	/** @return whether this box is axis-aligned (orientation a multiple of 90°). */
	private boolean isAxisAligned() {
		return angle_degree % 90 == 0;
	}

	/**
	 * Half-extent along world x for an axis-aligned box: the half-width at 0°/180°,
	 * the half-height at 90°/270° (where the box is turned on its side).
	 */
	private double extentX() {
		return angle_degree % 180 == 0 ? halfWidth : halfHeight;
	}

	/** Half-extent along world y for an axis-aligned box (the dual of {@link #extentX()}). */
	private double extentY() {
		return angle_degree % 180 == 0 ? halfHeight : halfWidth;
	}

	class RectRectIntersection {
		private Rect a, b;

		RectRectIntersection(Rect a, Rect b) {
			this.a = a;
			this.b = b;
		}

		boolean intersects() {
			ISU.Coord ca = a.getCenter();
			double[][] cornersA = a.cornersAt(ca.x(), ca.y());
			ISU.Vector delta = ca.nearestDeltaTo(b.getCenter());
			double[][] cornersB = b.cornersAt(ca.x() + delta.x(), ca.y() + delta.y());

			double[][] axes = { getEdgeNormal(cornersA[0], cornersA[1]), getEdgeNormal(cornersA[1], cornersA[2]),
					getEdgeNormal(cornersB[0], cornersB[1]), getEdgeNormal(cornersB[1], cornersB[2]) };

			for (double[] axis : axes) {
				double minA = project(cornersA, axis, false);
				double maxA = project(cornersA, axis, true);
				double minB = project(cornersB, axis, false);
				double maxB = project(cornersB, axis, true);
				if (maxA < minB || maxB < minA)
					return false;
			}
			return true;
		}

		private double[] getEdgeNormal(double[] p1, double[] p2) {
			double dx = p2[0] - p1[0];
			double dy = p2[1] - p1[1];
			double len = Math.hypot(dx, dy);
			if (len == 0)
				return new double[] { 0, 0 };
			return new double[] { -dy / len, dx / len };
		}

		private double project(double[][] corners, double[] axis, boolean max) {
			double val = max ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
			for (double[] c : corners) {
				double dot = c[0] * axis[0] + c[1] * axis[1];
				if (max) {
					if (dot > val)
						val = dot;
				} else {
					if (dot < val)
						val = dot;
				}
			}
			return val;
		}
	}

	// GETTERS
	public double getHalfWidth() {
		return halfWidth;
	}

	public double getHalfHeight() {
		return halfHeight;
	}

	public int getAngle() {
		return angle_degree;
	}

	public ISU.Coord getCenter() {
		return center;
	}

}