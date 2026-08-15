package frank.gal.aut.actions;

import engine.entities.Entity;
import engine.gal.aut.GALBot;
import engine.gal.aut.iGALAction;
import frank.entities.CherryProjectile;
import frank.entities.Enemy;
import frank.entities.Frank;
import frank.game.FruityFrankModel;
import frank.game.GameApp;
import frank.game.GameState;

// == HIT ==

/** GAL {@code Hit} action: the aggressor running this automaton damages the {@code $} target it is currently touching. */
public final class Hit implements iGALAction {

	/** The single shared instance — it is stateless. */
	public static final Hit INSTANCE = new Hit();

	private Hit() {
	}

	@Override
	public boolean exec(Entity self) {
		if (!(self.bot() instanceof GALBot bot))
			return false;
		Entity target = bot.selected(); // the GAL $ selection
		if (target == null || !(self.model() instanceof FruityFrankModel model))
			return false;

		// Guard a stale $: only hit a target that still exists and is actually in contact.
		if (!model.entities().contains(target) || !self.intersects(target)) {
			bot.select(null);
			return false;
		}

		GameState state = model.state();

		if (target instanceof Frank) {
			model.killFrank();
			return true;
		}

		if (target instanceof Enemy) {
			model.remove(target);
			state.addScore(self instanceof CherryProjectile ? 300 : 500);
			GameApp.audio.explosion();
			return true;
		}

		return false;
	}
}
