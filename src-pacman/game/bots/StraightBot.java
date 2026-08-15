package game.bots;

import engine.entities.Bot;
import engine.entities.Entity;
import engine.entities.Stunt;

// == STRAIGHT BOT ==

/**
 * A trivial {@link Bot} with a constant linear velocity (cm per tick) and no
 * rotation. Enough to demonstrate movement, torus wrap and collisions in the
 * demo and tests; real AI bots replace it. It pushes its constant command
 * immediately, so the entity must already have its stunt set when the bot is
 * constructed.
 */
public class StraightBot extends Bot {

	public StraightBot(Entity entity, double vx, double vy) {
		super(entity);
		Stunt stunt = stunt();
		if (stunt != null)
			stunt.drive(vx, vy);
	}
}
