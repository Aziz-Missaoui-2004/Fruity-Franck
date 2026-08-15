package engine.gal.aut;

import engine.entities.Entity;
import engine.gal.actions.Nothing;
import engine.gal.actions.Wait;
import engine.gal.conditions.And;
import engine.gal.conditions.Not;
import engine.gal.conditions.Or;
import engine.gal.conditions.True;
import frank.gal.aut.actions.Egg;
import frank.gal.aut.actions.Explode;
import frank.gal.aut.actions.Move;
import frank.gal.aut.actions.Turn;
import frank.gal.aut.conditions.Closest;
import frank.gal.aut.conditions.MyDir;
import frank.gal.aut.conditions.Step;

// == GAL (hand-built AST builder) ==

/**
 * Static factory facade for assembling GAL automata in Java, standing in for
 * the teachers' {@code .gal} parser until it lands (the parser will produce the
 * same {@link Automaton}/{@link Transition}/… types, so only this file
 * retires). Use with {@code import static engine.gal.aut.Gal.*;} so a built
 * automaton reads like its source.
 */
public final class Gal {

	private Gal() {
	}

	// CONDITIONS

	/** {@code True} — always satisfied. */
	public static iGALCondition true_() {
		return True.INSTANCE;
	}

	/**
	 * {@code Closest(cat)} — selects the nearest matching entity into {@code $}.
	 */
	public static iGALCondition closest(Category cat) {
		return new Closest(cat);
	}

	/**
	 * {@code Closest(cat, dir)} — selects the nearest; true iff it is in
	 * {@code dir}.
	 */
	public static iGALCondition closest(Category cat, Direction dir) {
		return new Closest(cat, dir);
	}

	/** {@code Closest(cat, range)} — nearest within {@code range} steps. */
	public static iGALCondition closest(Category cat, int rangeSteps) {
		return new Closest(cat, rangeSteps);
	}

	/**
	 * {@code Closest(cat, dir, range)} — nearest within range; true iff in
	 * {@code dir}.
	 */
	public static iGALCondition closest(Category cat, Direction dir, int rangeSteps) {
		return new Closest(cat, dir, rangeSteps);
	}

	/**
	 * {@code Step(dir, cat)} — is a {@code cat} entity one step away in
	 * {@code dir}?
	 */
	public static iGALCondition step(Direction dir, Category cat) {
		return new Step(dir, cat);
	}

	/** {@code Step(dir, n, cat)} — same, {@code n} steps away. */
	public static iGALCondition step(Direction dir, int n, Category cat) {
		return new Step(dir, cat, n);
	}

	/** {@code MyDir(dir)} — is the entity facing {@code dir}? */
	public static iGALCondition myDir(Direction dir) {
		return new MyDir(dir);
	}

	// CONDITION OPERATORS

	/** {@code not(c)}. */
	public static iGALCondition not(iGALCondition c) {
		return new Not(c);
	}

	/** {@code c1 & c2 & …}. */
	public static iGALCondition and(iGALCondition... cs) {
		return new And(cs);
	}

	/** {@code c1 / c2 / …}. */
	public static iGALCondition or(iGALCondition... cs) {
		return new Or(cs);
	}

	// DIRECTION ARGUMENTS

	/**
	 * A direction argument, resolved against the entity's facing at action time.
	 */
	public static DirArg dir(Direction d) {
		return self -> {
			Direction abs = d.resolve(Direction.ofAngle(self.orientation()));
			return new int[] { abs.dx(), abs.dy() };
		};
	}

	/**
	 * {@code $.d} — the cardinal direction from the entity toward its {@code $}.
	 */
	public static DirArg selectedDir() {
		return self -> {
			if (!(self.bot() instanceof GALBot b))
				return new int[] { 0, 0 };
			Entity sel = b.selected();
			if (sel == null)
				return new int[] { 0, 0 };
			var delta = self.center().nearestDeltaTo(sel.center());
			Direction c = Direction.cardinal(delta.x(), delta.y());
			return new int[] { c.dx(), c.dy() };
		};
	}

	/** {@code Opp(d)} — the opposite of another direction argument. */
	public static DirArg opp(DirArg inner) {
		return self -> {
			int[] d = inner.resolve(self);
			return new int[] { -d[0], -d[1] };
		};
	}

	// ACTIONS

	/** {@code Move(d)} — face {@code d} and push a velocity command. */
	public static iGALAction move(DirArg d) {
		return new Move(d);
	}

	/** {@code Turn(d)} — reorient to {@code d} without moving. */
	public static iGALAction turn(DirArg d) {
		return new Turn(d);
	}

	/**
	 * {@code Egg(d)} — lay the creator's default offspring on the cell in
	 * {@code d}.
	 */
	public static iGALAction egg(DirArg d) {
		return new Egg(d);
	}

	/** {@code Egg(cat, d)} — lay a {@code cat} entity on the cell in {@code d}. */
	public static iGALAction egg(Category cat, DirArg d) {
		return new Egg(d, cat);
	}

	/** {@code Explode} — self-destruct (remove from the world). */
	public static iGALAction explode() {
		return Explode.INSTANCE;
	}

	/** {@code Wait(n)} — active wait of {@code n} motion ticks (a timed action). */
	public static iGALAction wait_(int ticks) {
		return new Wait(ticks);
	}

	/** The no-op action for action-less transitions {@code | cond ? :(tgt)}. */
	public static iGALAction nothing() {
		return Nothing.INSTANCE;
	}

	// STRUCTURE

	public static State state(String mode) {
		return new State(mode);
	}

	/** The GAL destroy target {@code ()}. */
	public static State destroy() {
		return State.DESTROY;
	}

	/** The GAL random target {@code (_)}. */
	public static State random() {
		return State.RANDOM;
	}

	public static Transition on(State src, iGALCondition cond, iGALAction action, State tgt) {
		return new Transition(src, cond, action, tgt);
	}

	public static Automaton automaton(String name, State initial, Transition... transitions) {
		Automaton a = new Automaton(name, initial);
		for (Transition t : transitions)
			a.add(t);
		return a;
	}

	/**
	 * An idle automaton: one state, no transitions — it does nothing forever
	 * without dying (the GAL {@code Philosopher(Think){ *(Think) }}). The spec's
	 * default for décor/props so every entity carries an automaton that a game can
	 * later swap.
	 */
	public static Automaton idle(String name) {
		return new Automaton(name, new State(name));
	}
}
