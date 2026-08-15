package engine.gal.aut;

import engine.entities.Bot;
import engine.entities.Entity;

// == GAL BOT ==

/**
 * A {@link Bot} whose behaviour is a GAL {@link Automaton}. On each
 * {@link #tick} it steps the automaton against its entity; the FSM's
 * transitions read the world via conditions and actuate via the entity's stunt.
 * Carries the GAL {@code $} selection (set by conditions like {@code Closest})
 * and the per-game {@link CategoryMatcher} that binds abstract categories to
 * entities.
 */
public class GALBot extends Bot {

	private Automaton automaton;
	private final CategoryMatcher matcher;
	private final double speed; // cm per tick used by Move
	private Entity selected; // the GAL $ variable
	private EntityFactory factory; // game seam for Egg/Throw spawning (may be null)

	public GALBot(Entity entity, Automaton automaton, double speed, CategoryMatcher matcher) {
		super(entity);
		this.matcher = matcher;
		this.speed = speed;
		set(automaton);
	}

	/** Swap the automaton; the bot resets to its initial state. */
	public void set(Automaton automaton) {
		this.automaton = automaton;
		state(automaton == null ? null : automaton.initial());
	}

	@Override
	public void tick(double elapsed) {
		if (automaton != null)
			automaton.step(entity);
	}

	public CategoryMatcher matcher() {
		return matcher;
	}

	/**
	 * The game's entity factory used by {@code Egg}/{@code Throw}; may be
	 * {@code null}.
	 */
	public EntityFactory factory() {
		return factory;
	}

	public void setFactory(EntityFactory factory) {
		this.factory = factory;
	}

	public double speed() {
		return speed;
	}

	/** Bind the GAL {@code $} variable (the entity a condition just selected). */
	public void select(Entity e) {
		this.selected = e;
	}

	/** @return the GAL {@code $} selection, or {@code null}. */
	public Entity selected() {
		return selected;
	}
}
