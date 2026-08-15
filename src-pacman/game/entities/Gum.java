package game.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import game.Categorized;
import game.Category;

// == GUM ==

public class Gum extends Entity implements Categorized {

	/** A power pellet frightens the ghosts and lets Pac eat them. */
	private final boolean power;

	// CONSTRUCTOR

	public Gum(Game game, int x_ncell, int y_ncell) {
		this(game, x_ncell, y_ncell, false);
	}

	public Gum(Game game, int x_ncell, int y_ncell, boolean power) {
		super("Gum", game);
		this.power = power;
		setStep(isu.new Vector(0, 0));
		double sizeCm = (power ? 0.5 : 0.2) * isu.getCmPerCell();
		setSize(isu.new Dimension(sizeCm, sizeCm));
		setPosition(grid.new Position(x_ncell, y_ncell));
		setBounding(game);
	}

	/** @return whether this is a power pellet (vs a normal gum). */
	public boolean isPower() {
		return power;
	}

	// === Task COLLISION ===
	@Override
	protected void setBounding(Game game) {
		Circle circle = new Circle(center.mkCopy(), 0.5 * size.x());
		Bounding b = new Bounding(center.mkCopy());
		b.add(circle);
		setBounding(b);
	}

	@Override
	public Category category() {
		return Category.GUM;
	}

}
