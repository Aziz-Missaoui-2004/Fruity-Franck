package gal.ast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import gal.parser.Parser;

import engine.entities.Entity;
import engine.gal.aut.Automaton;
import engine.gal.aut.Category;
import engine.gal.aut.DirArg;
import engine.gal.aut.Direction;
import engine.gal.aut.GALBot;
import engine.gal.aut.State;
import engine.gal.aut.Transition;
import engine.gal.aut.iGALAction;
import engine.gal.aut.iGALCondition;
import engine.gal.actions.Nothing;
import engine.gal.actions.Wait;
import engine.gal.conditions.And;
import engine.gal.conditions.Not;
import engine.gal.conditions.Or;
import engine.gal.conditions.True;
import frank.gal.aut.actions.Egg;
import frank.gal.aut.actions.Explode;
import frank.gal.aut.actions.Hit;
import frank.gal.aut.actions.Move;
import frank.gal.aut.actions.Pick;
import frank.gal.aut.actions.Turn;
import frank.gal.aut.conditions.CanFall;
import frank.gal.aut.conditions.Closest;
import frank.gal.aut.conditions.Got;
import frank.gal.aut.conditions.MyDir;
import frank.gal.aut.conditions.Step;

/**
 * Converts the teachers' GAL parser AST (package gal.ast) to the engine GAL FSM
 * classes (package engine.gal.aut). This is the bridge between .gal files and GALBot.
 */
public final class GalCompiler {

    private GalCompiler() {}

    public static Map<String, Automaton> fromFile(String path) throws Exception {
        return compile(Parser.from_file(path));
    }

    public static Map<String, Automaton> compile(AST ast) {
        Map<String, Automaton> result = new LinkedHashMap<>();
        for (gal.ast.Automaton aut : ast.aut_list) {
            Automaton compiled = compileAutomaton(aut);
            result.put(compiled.name(), compiled);
        }
        return result;
    }

    public static Automaton compileAutomaton(gal.ast.Automaton src) {
        Automaton dst = new Automaton(src.name, state(src.initial_state));
        for (Mode mode : src.modes) {
            State source = state(mode.state);
            for (gal.ast.Transition tr : mode.behaviour.transitions) {
                dst.add(new Transition(
                    source,
                    condition(tr.condition.expression),
                    action(tr.action),
                    state(tr.target)
                ));
            }
        }
        return dst;
    }

    private static State state(gal.ast.State s) {
        if (s == null || s.name == null) return new State("Idle");
        if (s.name.equals("")) return State.DESTROY;  // ()
        if (s.name.equals("_")) return State.RANDOM;  // (_)
        return new State(s.name);
    }

    private static iGALCondition condition(Expression e) {
        if (e instanceof FunCall f) return conditionCall(f);
        if (e instanceof BinaryOp b) {
            iGALCondition l = condition(b.left_operand);
            iGALCondition r = condition(b.right_operand);
            return switch (b.operator) {
                case "&" -> new And(l, r);
                case "/" -> new Or(l, r);
                default -> throw unsupported("condition operator", b.operator);
            };
        }
        if (e instanceof UnaryOp u) {
            if (u.operator.equals("!")) return new Not(condition(u.operand));
            throw unsupported("unary condition operator", u.operator);
        }
        throw unsupported("condition", e.toString());
    }

    private static iGALCondition conditionCall(FunCall f) {
        String name = f.name;
        List<Parameter> p = f.parameters;
        return switch (name) {
            case "True" -> True.INSTANCE;
            case "Closest" -> closest(p);
            case "Step", "Cell" -> step(p);
            case "MyDir" -> new MyDir(direction(param(p, 0, name)));
            case "Struck" -> new CanFall(); // reuses the spare "Struck" token: gravity guard for fruits
            case "Got" -> new Got(category(param(p, 0, name))); // spare "Got": bounding-overlap with a category
            default -> throw unsupported("condition", f.toString());
        };
    }

    private static iGALCondition closest(List<Parameter> p) {
        Category cat = category(param(p, 0, "Closest"));
        if (p.size() == 1) return new Closest(cat);
        if (p.size() == 2) {
            Parameter p1 = p.get(1);
            if (p1 instanceof IntValue n) return new Closest(cat, n.value);
            return new Closest(cat, direction(p1));
        }
        if (p.size() == 3) return new Closest(cat, direction(p.get(1)), intValue(p.get(2)));
        throw unsupported("Closest parameters", p.toString());
    }

    private static iGALCondition step(List<Parameter> p) {
        Direction dir = direction(param(p, 0, "Step"));
        if (p.size() == 2) return new Step(dir, category(p.get(1)));
        if (p.size() == 3) return new Step(dir, category(p.get(2)), intValue(p.get(1)));
        throw unsupported("Step parameters", p.toString());
    }

    private static iGALAction action(Actions actions) {
        if (actions == null || actions.actions.isEmpty()) return Nothing.INSTANCE;
        List<iGALAction> list = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        for (FunCall f : actions.actions) {
            list.add(actionCall(f));
            weights.add(f.percent == FunCall.NO_PERCENT ? 100 : f.percent);
        }
        if (list.size() == 1) return list.get(0);
        return actions.operator.equals("/") ? new ChoiceAction(list, weights) : new SequenceAction(list);
    }

    private static iGALAction actionCall(FunCall f) {
        String name = f.name;
        List<Parameter> p = f.parameters;
        return switch (name) {
            case "Move" -> new Move(dirArg(param(p, 0, name)));
            case "Turn" -> new Turn(dirArg(param(p, 0, name)));
            case "Wait" -> new Wait(intValue(param(p, 0, name)));
            case "Egg" -> egg(p);
            case "Explode" -> Explode.INSTANCE;
            case "Pick" -> Pick.INSTANCE; // spare "Pick": award a fruit/cherry value
            case "Hit" -> Hit.INSTANCE;   // spare "Hit": apply the aggressor's effect to $
            default -> throw unsupported("action", f.toString());
        };
    }

    private static iGALAction egg(List<Parameter> p) {
        if (p.size() == 1) return new Egg(dirArg(p.get(0)));
        if (p.size() == 2) {
            // Prof syntax sometimes appears as Egg(F, Enemy), while our engine action is Egg(dir, category).
            return new Egg(dirArg(p.get(0)), category(p.get(1)));
        }
        throw unsupported("Egg parameters", p.toString());
    }

    private static Parameter param(List<Parameter> p, int index, String owner) {
        if (index >= p.size()) throw unsupported(owner + " parameters", p.toString());
        return p.get(index);
    }

    private static int intValue(Parameter p) {
        if (p instanceof IntValue i) return i.value;
        throw unsupported("integer parameter", p.toString());
    }

    private static Category category(Parameter p) {
        String name;
        if (p instanceof gal.ast.Category c) name = c.terminal.content;
        else name = p.toString();

        // Aliases from the subject + a couple of names useful for Fruity Frank.
        name = switch (name) {
            case "Adversary" -> "A";
            case "Clue" -> "C";
            case "Danger" -> "D";
            case "Gate" -> "G";
            case "Icon" -> "I";
            case "Jumpable" -> "J";
            case "Killable" -> "K";
            case "Moveable", "Missile" -> "M";
            case "Obstacle" -> "O";
            case "Pickable" -> "P";
            case "Team" -> "T";
            case "Util" -> "U";
            case "Void" -> "V";
            case "PlayerT" -> "@";
            case "PlayerA" -> "#";
            case "Enemy" -> "A";
            case "Frank" -> "@";
            default -> name;
        };
        Category cat = Category.canonical(name);
        if (cat == null) throw unsupported("category", p.toString());
        return cat;
    }

    private static Direction direction(Parameter p) {
        String name = p.toString();
        name = switch (name) {
            case "Here" -> "H";
            case "North" -> "N";
            case "South" -> "S";
            case "East" -> "E";
            case "West" -> "W";
            case "Forward" -> "F";
            case "BackWard", "Backward" -> "B";
            case "Left" -> "L";
            case "Right" -> "R";
            default -> name;
        };
        Direction d = Direction.canonical(name);
        if (d == null) throw unsupported("direction", p.toString());
        return d;
    }

    private static DirArg dirArg(Parameter p) {
        if (p instanceof Variable v) {
            DirArg base = selectedDir(v);
            return v.opposite ? opposite(base) : base;
        }
        Direction d = direction(p);
        return self -> {
            Direction abs = d.resolve(Direction.ofAngle(self.orientation()));
            return new int[] { abs.dx(), abs.dy() };
        };
    }

    private static DirArg selectedDir(Variable v) {
        if (v.index == 1 && v.type == v.DIRECTION) {
            return self -> {
                if (!(self.bot() instanceof GALBot b) || b.selected() == null) return new int[] {0, 0};
                var delta = self.center().nearestDeltaTo(b.selected().center());
                Direction c = Direction.cardinal(delta.x(), delta.y());
                return new int[] { c.dx(), c.dy() };
            };
        }
        throw unsupported("direction variable", v.toString());
    }

    private static DirArg opposite(DirArg inner) {
        return self -> {
            int[] d = inner.resolve(self);
            return new int[] { -d[0], -d[1] };
        };
    }

    private static IllegalArgumentException unsupported(String kind, String value) {
        return new IllegalArgumentException("Unsupported GAL " + kind + ": " + value);
    }

    private static final class SequenceAction implements iGALAction {
        private final List<iGALAction> actions;
        SequenceAction(List<iGALAction> actions) { this.actions = actions; }
        @Override public boolean exec(Entity e) {
            for (iGALAction a : actions) if (!a.exec(e)) return false;
            return true;
        }
    }

    private static final class ChoiceAction implements iGALAction {
        private final List<iGALAction> actions;
        private final List<Integer> weights;
        ChoiceAction(List<iGALAction> actions, List<Integer> weights) { this.actions = actions; this.weights = weights; }
        @Override public boolean exec(Entity e) {
            int total = 0;
            for (int w : weights) if (w > 0) total += w;
            int r = total <= 0 ? 0 : ThreadLocalRandom.current().nextInt(total);
            for (int i = 0; i < actions.size(); i++) {
                int w = Math.max(0, weights.get(i));
                if (r < w) return actions.get(i).exec(e);
                r -= w;
            }
            return actions.get(0).exec(e);
        }
    }
}