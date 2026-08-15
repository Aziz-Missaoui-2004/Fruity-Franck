package engine.shapes;

import engine.geometry.ISU;

public abstract class Shape implements iShape {

	protected ISU isu;
	protected ISU.Coord center;

	public Shape(ISU.Coord center) {
		isu = center.getISU();
		this.setCenter(center);
	}

	@Override
	abstract public boolean intersects(iShape shape);

	@Override
	public boolean intersects(Circle circle) {
		return circle.intersects(this);
	}

	@Override
	public boolean intersects(Rect rect) {
		return rect.intersects(this);
	}

	public void setCenter(ISU.Coord center) {
		this.center = center;
	}

	/**
	 * @return the center
	 */
	public ISU.Coord getCenter() {
		return center;
	}

}