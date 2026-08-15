package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import frank.game.Categorized;
import frank.game.Category;

public class Enemy extends Entity implements Categorized {

	public enum EnemyType {
	    ORANGE,  // ligne 0
	    PURPLE,  // ligne 1
	    GREEN,   // ligne 2
	    RED      // ligne 3
	}

    private final EnemyType type;

    public Enemy(String name, Game game, int x, int y, EnemyType type) {
        super(name, game);
        this.type = type;
        //setStep(isu.new Vector(1.0, 1.0));
        setSize(grid.new Dimension(1, 1).toISUDimension());
        setPosition(grid.new Position(x, y));
        setBounding(game);
    }

    @Override
    protected void setBounding(Game game) {
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Circle(center.mkCopy(), 0.35 * game.getCmPerCell()));
        setBounding(b);
    }

    public EnemyType type() { return type; }

    public boolean canDig() {
        return type == EnemyType.RED || type == EnemyType.ORANGE;
    }

    @Override
    public Category category() { return Category.ENEMY; }
}