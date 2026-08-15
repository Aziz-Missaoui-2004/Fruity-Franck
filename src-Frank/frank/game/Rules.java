package frank.game;

import java.util.EnumMap;
import engine.entities.Entity;
import engine.geometry.Grid;
import frank.entities.Apple;
import frank.entities.Frank;
import frank.entities.GrassLow;

public class Rules {

	@FunctionalInterface
	public interface Outcome {
		void apply(FruityFrankModel m, GameState s, Entity self, Entity other);
	}

	private final EnumMap<Category, EnumMap<Category, Outcome>> table = new EnumMap<>(Category.class);

	public Rules() {
		// Frank touche un ennemi → perd une vie
		on(Category.FRANK, Category.ENEMY, (m, s, frank, enemy) -> {
			m.killFrank();
		});

		// Cerise projectile touche un ennemi → ennemi meurt
		on(Category.CHERRY_PROJECTILE, Category.ENEMY, (m, s, proj, enemy) -> {
			m.remove(enemy);
			m.remove(proj);
			s.addScore(300);
			GameApp.audio.explosion();  
		});

		// Pomme EN CHUTE qui rattrape Frank → Frank perd une vie.
		// Inoffensive pendant sa pause posée sur un gazon fin (isLowArmed).
		on(Category.APPLE, Category.FRANK, (m, s, apple, frank) -> {
			Apple a = (Apple) apple;
			if (canCrush(a)) {
				m.killFrank();
			}
		});

		// Pomme EN CHUTE qui écrase un ennemi → ennemi meurt.
		// Inoffensive pendant sa pause posée sur un gazon fin (isLowArmed).
		on(Category.APPLE, Category.ENEMY, (m, s, apple, enemy) -> {
			Apple a = (Apple) apple;
			if (canCrush(a)) {
				m.remove(enemy);
				s.addScore(500);
				 GameApp.audio.explosion();
			}
		});

		// Pomme EN CHUTE qui touche le gazon fin → marque une courte pause posée dessus
		// (contact = la pomme est pile à la bonne hauteur). Le décompte et l'écrasement
		// sont gérés par la garde CanFall ; la règle ne fait qu'armer et stopper.
		on(Category.APPLE, Category.GRASS_LOW, (m, s, apple, grass) -> {
			Apple a = (Apple) apple;

			if (!a.isFalling() || a.isLowArmed())
				return;

			var speed = a.getLinearSpeed();
			boolean movingDown = speed != null && speed.y() > 0;
			if (!movingDown)
				return;

			a.setCoord(grass.position().toISUCoordCentered());
			a.setFalling(false);
			a.armLow(Apple.LOW_PAUSE_TICKS);

			if (a.stunt() != null)
				a.stunt().drive(0, 0);
		});
	}

	private boolean canCrush(Apple apple) {
		var speed = apple.getLinearSpeed();
		boolean movingDown = speed != null && speed.y() > 0;
		return apple.isFalling() && !apple.isLowArmed() && !apple.hasVisualOffset()
				&& movingDown && !touchingGrassLow(apple);
	}

	private boolean touchingGrassLow(Apple apple) {
		for (Grid.Cell cell : apple.getHitbox().occupiedCells())
			for (Entity e : cell.entities())
				if (e instanceof GrassLow && apple.intersects(e))
					return true;
		return false;
	}

	public void resolve(FruityFrankModel model, GameState state, Entity a, Entity b) {
		if (apply(model, state, a, b))
			return;
		apply(model, state, b, a);
	}

	private void on(Category self, Category other, Outcome o) {
		table.computeIfAbsent(self, k -> new EnumMap<>(Category.class)).put(other, o);
	}

	private boolean apply(FruityFrankModel m, GameState s, Entity self, Entity other) {
		var row = table.get(Category.of(self));
		if (row == null)
			return false;
		var o = row.get(Category.of(other));
		if (o == null)
			return false;
		o.apply(m, s, self, other);
		return true;
	}
}
