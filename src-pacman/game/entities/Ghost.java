package game.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.geometry.ISU.Coord;
import engine.geometry.ISU.Dimension;
import engine.shapes.Circle;
import engine.shapes.Rect;
import game.Categorized;
import game.Category;

// == GHOST ==

public class Ghost extends Entity implements Categorized {

	// CONSTRUCTOR

	public Ghost(String name, Game game, int x_ncell, int y_ncell) {
		super(name, game);
		setStep(isu.new Vector(1.0, 1.0));
		setSize(grid.new Dimension(1, 1).toISUDimension());
		setPosition(grid.new Position(x_ncell, y_ncell));
		setBounding(game);
	}

	// === Task COLLISION ===

	@Override
	protected void setBounding(Game game) {
		double cmPerCell = game.getCmPerCell();
		double halfCell = cmPerCell / 2.0;

		Circle circle = new Circle(center.mkCopy(), halfCell);

		double rectWidth = cmPerCell;
		double rectHeight = halfCell;
		Coord rectCenter = isu.new Coord(center.x(), center.y() + rectHeight / 2.0);
		Dimension rectSize = isu.new Dimension(rectWidth, rectHeight);
		Rect rect = new Rect(rectCenter, rectSize, 0);

		Bounding bounding = new Bounding(center.mkCopy());
		bounding.add(circle);
		bounding.add(rect);
		setBounding(bounding);
	}

	@Override
	public Category category() {
		return Category.GHOST;
	}

}
