package engine.shapes;

import engine.geometry.ISU;

// = Circle =

public class Circle extends Shape {

	private double radius;

	// CONSTRUCTOR

	public Circle(ISU.Coord center, double radius) {
		super(center);
		this.radius = radius;

	}

	// INTERSECTION

	@Override
	public boolean intersects(iShape shape) {
		return shape.intersects(this);
	}

	@Override
	public boolean intersects(Rect rect) {
		return rect.intersects(this);
	}

	@Override
	public boolean intersects(Circle circle) {
		return getCenter().distanceTo(circle.getCenter()) <= radius + circle.getRadius();
	}

	// GETTERS

	public double getRadius() {
		return radius;
	}

	public ISU.Coord getCenter() {
		return center;
	}

}
