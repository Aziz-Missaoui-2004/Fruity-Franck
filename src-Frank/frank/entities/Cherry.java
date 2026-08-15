package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import frank.game.Categorized;
import frank.game.Category;

public class Cherry extends Entity implements Categorized {

    public Cherry(Game game, int x, int y) {
        super("Cherry@" + x + "," + y, game);
        setSize(grid.new Dimension(1, 1).toISUDimension());
        setPosition(grid.new Position(x, y));
        setBounding(game);
    }

    @Override
    protected void setBounding(Game game) {
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Circle(center.mkCopy(), 0.28 * game.getCmPerCell()));
        setBounding(b);
    }

    @Override
    public Category category() { return Category.CHERRY; }
}