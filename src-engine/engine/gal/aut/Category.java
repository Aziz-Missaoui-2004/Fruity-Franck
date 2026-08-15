package engine.gal.aut;

import java.util.HashMap;
import java.util.Map;

// == CATEGORY ==

/**
 * An abstract GAL category — the letters a scenarist writes ({@code A} =
 * adversary, {@code T} = team, {@code O} = obstacle, …). Interned by name
 * ({@code canonical("A") == Adversary}) and indexed into a symmetric
 * {@code interactsWith} table. Binding a category to <i>which concrete game
 * entities match it</i> is the game's job (see {@link CategoryMatcher}); this
 * class only models the abstract letters and their interaction relation.
 */
public final class Category {

	private static final Map<String, Category> CANON = new HashMap<>();
	private static int count = 0;
	private static final int MAX = 32;
	private static final boolean[][] interaction = new boolean[MAX][MAX];

	// The standard GAL letters (see PLE_2026_GAL_partie2). Semantics are
	// conventional;
	// what concrete entities match each letter is the game's job (CategoryMatcher).
	public static final Category Adversary = intern("A");
	public static final Category Clue = intern("C");
	public static final Category Danger = intern("D");
	public static final Category Gate = intern("G");
	public static final Category Icon = intern("I");
	public static final Category Jumpable = intern("J");
	public static final Category Killable = intern("K");
	public static final Category Missile = intern("M");
	public static final Category Obstacle = intern("O");
	public static final Category Pick = intern("P");
	public static final Category Team = intern("T");
	public static final Category Util = intern("U");
	public static final Category Void = intern("V");
	// Letters without imposed semantics, for game-specific use.
	public static final Category Qube = intern("Q");
	public static final Category Xkcs = intern("X");
	public static final Category Ygre = intern("Y");
	public static final Category Zedd = intern("Z");
	// Distinguished singletons.
	public static final Category Player = intern("@"); // the player of my team
	public static final Category Boss = intern("#"); // the boss of the other team

	private final String name;
	private final int index;

	private Category(String name, int index) {
		this.name = name;
		this.index = index;
	}

	private static Category intern(String name) {
		Category c = new Category(name, count++);
		CANON.put(name, c);
		return c;
	}

	/**
	 * @return the interned category for {@code name}, or {@code null} if unknown.
	 */
	public static Category canonical(String name) {
		return CANON.get(name);
	}

	/** Record (symmetrically) that {@code c1} and {@code c2} interact. */
	public static void setInteraction(Category c1, Category c2, boolean b) {
		interaction[c1.index][c2.index] = b;
		interaction[c2.index][c1.index] = b;
	}

	public boolean interactsWith(Category c) {
		return interaction[this.index][c.index];
	}

	public String name() {
		return name;
	}

	public int index() {
		return index;
	}

	@Override
	public String toString() {
		return name;
	}
}
