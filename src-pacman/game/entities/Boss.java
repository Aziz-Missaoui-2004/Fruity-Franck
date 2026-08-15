package game.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.geometry.Grid.Position;
import engine.geometry.ISU.Coord;
import engine.geometry.Grid.Dimension;
import engine.shapes.Rect;
import game.Categorized;
import game.Category;

// = BOSS =

/**
//  X
//  *XX   |--X--X--*--|
//  X
//  X
*/

/**
 * @implNote Le Boss a la forme d'un `t`
 * @implNote il occupe 1 cellule au dessus de son centre, 2 au dessous de son
 *           centre, et 2 cellules à droite de son centre.
 * @implNote Le Boss a donc une dimension (x=3Cell,y=4Cell)
 * @implNote Cette forme a été choisie pour pouvoir tester les rotations.
 */
public class Boss extends Entity implements Categorized {

	// CONSTRUCTOR

	public Boss(String name, Game game, int x_ncell, int y_ncell) {
		super(name, game);
		setStep(isu.new Vector(1.0, 1.0));
		setSize(isu.new Dimension(3.0 * game.getCmPerCell(), 4.0 * game.getCmPerCell()));
		setPosition(grid.new Position(x_ncell, y_ncell));
		setBounding(game);
	}

	@Override
	protected void setBounding(Game game) {
		Position p1 = grid.new Position(position.x() + 1, position.y());
		Dimension d1 = grid.new Dimension(3, 1);
		Rect rect1 = new Rect(p1.toISUCoordCentered(), d1.toISUDimension(), 0);

		Coord c2 = isu.new Coord(center.x(), 0.5 * game.getCmPerCell() + center.y());
		Dimension d2 = grid.new Dimension(1, 4);
		Rect rect2 = new Rect(c2, d2.toISUDimension(), 0);

		Bounding b = new Bounding(center.mkCopy()); // copy anchor
		b.add(rect1);
		b.add(rect2);
		setBounding(b);
	}

	@Override
	public Category category() {
		return Category.BOSS;
	}

}
