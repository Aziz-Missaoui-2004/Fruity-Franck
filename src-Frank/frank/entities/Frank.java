package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import frank.game.Categorized;
import frank.game.Category;

public class Frank extends Entity implements Categorized {

	private boolean hasCherry = false;
	private int facingDx = 1, facingDy = 0;

	public static final int DEATH_TICKS = 30;
	public static final int INVINCIBLE_TICKS = 90; // 3 secondes si MOTION_TPS = 30

	private int dyingTicks = -1;
	private boolean lastDeath = false;

	private int invincibleTicks = 0;

	public Frank(String name, Game game, int x, int y) {
		super(name, game);
		setStep(isu.new Vector(1.0, 1.0));
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

	public boolean hasCherry() { return hasCherry; }
	public void pickCherry() { hasCherry = true; }
	public void useCherry() { hasCherry = false; }

	public int facingDx() { return facingDx; }
	public int facingDy() { return facingDy; }

	public void setFacing(int dx, int dy) {
		if (dx != 0 || dy != 0) {
			facingDx = dx;
			facingDy = dy;
		}
	}

	public boolean isDying() { return dyingTicks >= 0; }
	public boolean isLastDeath() { return lastDeath; }

	public void startDying(boolean last) {
		if (dyingTicks >= 0) return;
		dyingTicks = DEATH_TICKS;
		lastDeath = last;
	}

	public void tickDying() {
		if (dyingTicks > 0) dyingTicks--;
	}

	public boolean dyingDone() {
		return dyingTicks == 0;
	}

	public void clearDying() {
		dyingTicks = -1;
		lastDeath = false;
	}

	public void makeInvincible() {
		invincibleTicks = INVINCIBLE_TICKS;
	}

	public boolean isInvincible() {
		return invincibleTicks > 0;
	}

	public void tickInvincible() {
		if (invincibleTicks > 0)
			invincibleTicks--;
	}

	public boolean blinkVisible() {
		if (dyingTicks >= 0) {
			if (lastDeath) return false;
			int elapsed = DEATH_TICKS - dyingTicks;
			int half = Math.max(1, DEATH_TICKS / 6);
			return (elapsed / half) % 2 == 0;
		}

		if (isInvincible()) {
			return (invincibleTicks / 5) % 2 == 0;
		}

		return true;
	}

	@Override
	public Category category() {
		return Category.FRANK;
	}
}