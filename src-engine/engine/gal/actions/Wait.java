package engine.gal.actions;

import engine.entities.Entity;
import engine.entities.Stunt;
import engine.gal.aut.iGALAction;

// == WAIT ==

/**
 * The GAL {@code Wait(n)} action: active waiting that takes time (n motion
 * ticks). It reports failure while the wait is in progress, so a self-guarded
 * transition {@code | True ? Wait(n) :(Next)} stays put until the timer
 * elapses, then succeeds once and lets the FSM advance.
 */
public final class Wait implements iGALAction {

	private final int ticks;

	public Wait(int ticks) {
		this.ticks = ticks;
	}

	@Override
	public boolean exec(Entity self) {
		Stunt stunt = self.stunt();
		if (stunt == null)
			return true; // nothing to wait on — treat as instantly done
		if (stunt.consumeWaitDone())
			return true; // the armed wait has elapsed
		if (!stunt.busy())
			stunt.beginWait(ticks); // start it
		return false; // still waiting
	}
}
