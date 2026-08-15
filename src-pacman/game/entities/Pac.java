package game.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import game.Categorized;
import game.Category;

public class Pac extends Entity implements Categorized {

	/** Whether Pac currently has a power-up (eats ghosts instead of dying). */
	private boolean powered = false;

	public Pac(String name, Game game, int x_ncell, int y_ncell) {
		super(name, game);
		setStep(isu.new Vector(1.0, 1.0));
		setSize(grid.new Dimension(1, 1).toISUDimension());
		setPosition(grid.new Position(x_ncell, y_ncell));
		setBounding(game);
	}

	public Pac(String name, Game game, double x_cm, double y_cm) {
		super(name, game);
		setStep(isu.new Vector(1.0, 1.0));
		setSize(grid.new Dimension(1, 1).toISUDimension());
		setCoord(isu.new Coord(x_cm, y_cm));
		setBounding(game);
	}

	@Override
	protected void setBounding(Game game) {
		Circle circle = new Circle(center.mkCopy(), 0.5 * game.getCmPerCell());
		Bounding b = new Bounding(center.mkCopy());
		b.add(circle);
		setBounding(b);
	}

	// POWER-UP

	public boolean isPowered() {
		return powered;
	}

	public void setPowered(boolean powered) {
		this.powered = powered;
	}

	@Override
	public Category category() {
		return Category.PAC;
	}

}
