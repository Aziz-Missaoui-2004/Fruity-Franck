package engine.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;

// = BOUNDING =

import java.util.Set;

import engine.geometry.ISU;
import engine.geometry.ISU.Coord;
import engine.shapes.Rect;
import engine.shapes.Shape;
import engine.shapes.iShape;

public class Bounding {

	// FIELDS

	Set<iShape> boundings;
	private Coord anchor;

	// CONSTRUCTOR

	/**
	 * @param anchor the pivot point for rotations. May be the entity center or any
	 *               other point. Will be translated automatically alongside the
	 *               entity. If the anchor is the same point as the entity's center
	 *               use a copy.
	 */
	public Bounding(ISU.Coord anchor) {
		boundings = new HashSet<>();
		this.anchor = anchor;
	}

	// BUILDER

	public void add(iShape shape) {
		boundings.add(shape);
	}

	// INTERSECTION

	public boolean intersects(iShape shape) {
		for (iShape s : boundings) {
			if (shape.intersects(s))
				return true;
		}

		return false;
	}

	boolean intersects(Bounding bounding) {
		for (iShape s : boundings) {
			if (bounding.intersects(s))
				return true;
		}
		return false;
	}

	/**
	 * Translate the whole bounding (anchor and every shape centre) by
	 * {@code delta}.
	 *
	 * @implNote Each {@link ISU.Coord} is moved <b>exactly once</b>, identified by
	 *           reference (not by value). This lets a game developer safely reuse
	 *           the entity's own {@code center} object as the anchor and/or as a
	 *           shape centre: a point shared between the anchor and one or more
	 *           shapes is found a single time, so it is never translated twice.
	 *           Passing a defensive copy still works — it is simply a distinct
	 *           point that gets translated on its own.
	 */
	public void translate(ISU.Vector delta) {
		for (ISU.Coord point : distinctPoints())
			point.translate(delta);
	}

	/**
	 * Rotate the whole bounding by {@code angle_degree} around the (fixed)
	 * {@code anchor}.
	 *
	 * @implNote As in {@link #translate}, each distinct shape centre is rotated
	 *           once (by reference). The anchor is the pivot and never moves, so it
	 *           is skipped even when it is shared with a shape centre. Oriented
	 *           shapes additionally spin about their own centre.
	 */
	public void rotate(int angle_degree) {
		for (ISU.Coord center : distinctShapeCenters()) {
			if (center != anchor)
				center.rotateAround(anchor, angle_degree);
		}
		for (iShape s : boundings) {
			if (s instanceof Rect)
				((Rect) s).rotate(angle_degree);
		}
	}

	/**
	 * @return the anchor plus every shape centre, de-duplicated by object identity
	 *         so a point referenced more than once appears only once.
	 */
	private Set<ISU.Coord> distinctPoints() {
		Set<ISU.Coord> points = distinctShapeCenters();
		points.add(anchor);
		return points;
	}

	/**
	 * @return every shape centre, de-duplicated by object identity.
	 */
	private Set<ISU.Coord> distinctShapeCenters() {
		Set<ISU.Coord> centers = Collections.newSetFromMap(new IdentityHashMap<>());
		for (iShape s : boundings) {
			if (s instanceof Shape)
				centers.add(((Shape) s).getCenter());
		}
		return centers;
	}

	public Set<iShape> getShapes() {
		return boundings;
	}

	public void setAnchor(Coord c) {
		anchor = c;
	}

}
