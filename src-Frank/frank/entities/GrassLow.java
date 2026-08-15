package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.geometry.ISU;
import engine.shapes.Rect;
import frank.game.Categorized;
import frank.game.Category;

public class GrassLow extends Entity implements Categorized {

    public GrassLow(Game game, int x, int y) {
        super("GL@" + x + "," + y, game);
        double cm = game.getCmPerCell();
        setSize(isu.new Dimension(0.1 * cm, 0.1 * cm));
        setPosition(grid.new Position(x, y));
        // Décale le centre vers le BAS : la couche 1cm occupe le bas de la cellule
        ISU.Coord shifted = isu.new Coord(center.x(), center.y() + 0.45 * cm);
        setCoord(shifted);
        setBounding(game);
    }

    @Override
    protected void setBounding(Game game) {
        double cm = game.getCmPerCell();
        // 1cm de haut, presque toute la largeur
        ISU.Dimension size = isu.new Dimension(0.95 * cm, 0.1 * cm);
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Rect(center.mkCopy(), size, 0));
        setBounding(b);
    }

    @Override
    public Category category() { return Category.GRASS_LOW; }
}