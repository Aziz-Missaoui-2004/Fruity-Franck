package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.geometry.ISU;
import engine.shapes.Rect;
import frank.game.Categorized;
import frank.game.Category;

public class GrassHigh extends Entity implements Categorized {

    public static final int FADE_TICKS = 8;
    private int fadeTimer = -3;

    public GrassHigh(Game game, int x, int y) {
        super("GH@" + x + "," + y, game);
        setSize(grid.new Dimension(1, 1).toISUDimension());
        setPosition(grid.new Position(x, y));
        // Décale le centre vers le HAUT : la couche 9cm occupe le haut de la cellule
        // Cellule de hauteur cmPerCell, couche 9cm centrée à -0.05*cmPerCell du centre
        double cm = game.getCmPerCell();
        ISU.Coord shifted = isu.new Coord(center.x(), center.y() - 0.05 * cm);
        setCoord(shifted);
        setBounding(game);
    }

    @Override
    protected void setBounding(Game game) {
        double cm = game.getCmPerCell();
        // 9cm de haut, presque toute la largeur
        ISU.Dimension size = isu.new Dimension(0.95 * cm, 0.9 * cm);
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Rect(center.mkCopy(), size, 0));
        setBounding(b);
    }

    public boolean isFading() { return fadeTimer >= 0; }
    public void startFade()   { if (fadeTimer < 0) fadeTimer = FADE_TICKS; }
    public void tickFade()    { if (fadeTimer > 0) fadeTimer--; }
    public boolean fadeDone() { return fadeTimer == 0; }
    public int fadeTimer()    { return fadeTimer; }

    @Override
    public Category category() { return Category.GRASS_HIGH; }
}