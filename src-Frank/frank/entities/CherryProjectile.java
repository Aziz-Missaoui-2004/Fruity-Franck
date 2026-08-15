package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import frank.game.Categorized;
import frank.game.Category;

public class CherryProjectile extends Entity implements Categorized {
	private int cellsTravelled = 0;
	private int lastCol, lastRow;
	private static final int MAX_RANGE = 10;   // portée en cases (tore X = 21 large, donc < 21)
    private final int dx, dy;

    public CherryProjectile(Game game, int x, int y, int dx, int dy) {
        super("CherryProj@" + x + "," + y, game);
        this.dx = dx; this.dy = dy;
        setStep(isu.new Vector(1.0, 1.0));
        setSize(grid.new Dimension(1, 1).toISUDimension());
        setPosition(grid.new Position(x, y));
        lastCol = x;
        lastRow = y;
        setBounding(game);
    }

    @Override
    protected void setBounding(Game game) {
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Circle(center.mkCopy(), 0.18 * game.getCmPerCell()));
        setBounding(b);
    }
    public boolean outOfRange() {
        int c = position().x();
        int r = position().y();
        if (c != lastCol || r != lastRow) {   // on a changé de case
            cellsTravelled++;
            lastCol = c;
            lastRow = r;
        }
        return cellsTravelled >= MAX_RANGE;
    }

    public int facingDx() { return dx; }
    public int facingDy() { return dy; }

    @Override
    public Category category() { return Category.CHERRY_PROJECTILE; }
}