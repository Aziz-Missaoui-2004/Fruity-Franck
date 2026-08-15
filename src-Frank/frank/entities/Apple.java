package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import frank.game.Categorized;
import frank.game.Category;

public class Apple extends Entity implements Categorized {




    /** AI ticks the apple pauses on a thin floor (GrassLow) before crushing it (~0.5 s). */
    public static final int LOW_PAUSE_TICKS = 4;

    private boolean falling = false;
    private int fallDelay = -1; // -1 = pas armé
    private int lowDelay = -1;  // -1 = pas en train d'écraser une ligne fine
    private double visualOffsetX = 0;
    private double visualOffsetY = 0;
    private double visualSpeed = 0;
    private boolean pushFallStepPending = false;

    public Apple(Game game, int x, int y) {
        super("Apple@" + x + "," + y, game);
        setStep(isu.new Vector(1.0, 1.0));
        setSize(grid.new Dimension(1, 1).toISUDimension());
        setPosition(grid.new Position(x, y));
        setBounding(game);
    }


    public double visualOffsetX() { return visualOffsetX; }
    public double visualOffsetY() { return visualOffsetY; }

    public void startPushVisual(int dir, double cellCm, double speed) {
        visualOffsetX = -dir * cellCm;
        visualOffsetY = 0;
        visualSpeed = Math.abs(speed);
    }

    public void startFallVisual(double cellCm, double speed) {
        visualOffsetX = 0;
        visualOffsetY = -cellCm;
        visualSpeed = Math.abs(speed);
    }

    public void tickPushVisual() {
        if (visualOffsetX == 0 && visualOffsetY == 0)
            return;

        visualOffsetX = moveOffsetTowardZero(visualOffsetX);
        visualOffsetY = moveOffsetTowardZero(visualOffsetY);

        if (visualOffsetX == 0 && visualOffsetY == 0)
            visualSpeed = 0;
    }

    public boolean hasVisualOffset() {
        return visualOffsetX != 0 || visualOffsetY != 0;
    }

    public void schedulePushFallStep() {
        pushFallStepPending = true;
    }

    public boolean consumePushFallStep() {
        if (!pushFallStepPending)
            return false;
        pushFallStepPending = false;
        return true;
    }

    private double moveOffsetTowardZero(double offset) {
        if (offset == 0)
            return 0;

        double dir = Math.signum(offset);
        double next = offset - dir * visualSpeed;
        if (Math.signum(next) != dir)
            return 0;
        return next;
    }


    @Override
    protected void setBounding(Game game) {
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Circle(center.mkCopy(), 0.40 * game.getCmPerCell()));
        setBounding(b);
    }

    // LOW
    public boolean isLowArmed() {
        return lowDelay >= 0;
    }

    public void armLow(int ticks) {
        if (lowDelay < 0)
            lowDelay = ticks;
    }

    public void disarmLow() {
        lowDelay = -1;
    }

    public void tickLowDelay() {
        if (lowDelay > 0)
            lowDelay--;
    }

    public boolean lowDelayDone() {
        return lowDelay == 0;
    }
    
    public boolean isFalling()        { return falling; }
    public void setFalling(boolean f) { falling = f; }

    public boolean isArmed()   { return fallDelay >= 0; }
    public void arm(int ticks) { if (fallDelay < 0) fallDelay = ticks; }
    public void disarm()       { fallDelay = -1; }
    public void tickDelay()    { if (fallDelay > 0) fallDelay--; }
    public boolean delayDone() { return fallDelay == 0; }
    public int fallDelay()     { return fallDelay; }

    @Override
    public Category category() { return Category.APPLE; }
}
