package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import frank.game.Categorized;
import frank.game.Category;

public class Fruit extends Entity implements Categorized {

    public enum FruitType { BANANA, ORANGE, STRAWBERRY, PLUM, GRAPES }
    private final FruitType type;

    public Fruit(Game game, int x, int y, FruitType type) {
        super("Fruit_" + type + "@" + x + "," + y, game);
        this.type = type;
        setSize(grid.new Dimension(1, 1).toISUDimension());
        setPosition(grid.new Position(x, y));
        setBounding(game);
    }

    @Override
    protected void setBounding(Game game) {
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Circle(center.mkCopy(), 0.32 * game.getCmPerCell()));
        setBounding(b);
    }

    public FruitType type() { return type; }

    @Override
    public Category category() { return Category.FRUIT; }
}