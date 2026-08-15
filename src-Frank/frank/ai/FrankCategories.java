package frank.ai;

import engine.entities.Entity;
import engine.gal.aut.Category;
import engine.gal.aut.CategoryMatcher;
import frank.entities.Apple;
import frank.entities.Cherry;
import frank.entities.Enemy;
import frank.entities.Frank;
import frank.entities.Fruit;
import frank.entities.GrassHigh;
import frank.entities.GrassLow;

/**
 * Viewpoint-aware binding between abstract GAL category letters and concrete
 * Fruity Frank entities — the matcher half of AUTOMATA_PLAN §3a. The
 * interpreter asks "does {@code other} count as {@code cat} from {@code self}'s
 * point of view?"; conditions like {@code Closest}/{@code Step}/{@code Got} use
 * the answer to find or react to entities.
 *
 * <p>
 * Only {@code A} is relational (an adversary depends on who is asking); the rest
 * are plain type tests, two of them dynamic ({@code M}/{@code O}/{@code D} look
 * at an apple's {@code isFalling()} state). The mapping is a superset of the old
 * "A → Frank" rule: enemies still see Frank as their adversary, so existing
 * enemy automata are unaffected.
 */
public class FrankCategories implements CategoryMatcher {

    @Override
    public boolean matches(Category cat, Entity self, Entity other) {
        // @ Player : Frank.
        if (cat == Category.Player)
            return other instanceof Frank;

        // A Adversary : the other team. Frank's adversary is an Enemy; everyone
        // else's adversary is Frank. (Frank has no automaton today; enemies use
        // Closest(A) to track Frank, which this preserves.)
        if (cat == Category.Adversary)
            return (self instanceof Frank) ? other instanceof Enemy : other instanceof Frank;

        // K Killable : an Enemy (target of a cherry shot or a falling apple).
        if (cat == Category.Killable)
            return other instanceof Enemy;

        // P Pickable : Fruit or Cherry.
        if (cat == Category.Pick)
            return other instanceof Fruit || other instanceof Cherry;

        // M Missile : an apple while it is falling (dynamic).
        if (cat == Category.Missile)
            return other instanceof Apple a && a.isFalling();

        // O Obstacle : the thick grass, plus a resting (non-falling) apple.
        if (cat == Category.Obstacle)
            return other instanceof GrassHigh || (other instanceof Apple a && !a.isFalling());

        // D Danger : whatever hurts Frank — an Enemy or a falling apple.
        if (cat == Category.Danger)
            return other instanceof Enemy || (other instanceof Apple a && a.isFalling());

        // G : the thin grass floor (crushed by a digger or a falling apple).
        if (cat == Category.Gate)
            return other instanceof GrassLow;

        return false;
    }
}
