package engine.gal.aut;

import java.util.HashMap;
import java.util.Map;

// == DIRECTION ==

/**
 * A GAL direction constant, interned by name (so {@code canonical("N") == N}).
 * Absolute directions (N/S/E/W) carry a screen-space unit step (y grows
 * <i>downward</i>, so N = -y) and a nominal angle; relative directions
 * (H/F/B/L/R) are resolved against an entity's facing by the interpreter.
 */
public final class Direction {

	private static final Map<String, Direction> CANON = new HashMap<>();

	// Absolute (screen convention: y downward -> N is -y).
	public static final Direction N = abs("N", 0, -1, 90);
	public static final Direction S = abs("S", 0, 1, 270);
	public static final Direction E = abs("E", 1, 0, 0);
	public static final Direction W = abs("W", -1, 0, 180);

	// Relative (resolved against the entity's facing
	public static final Direction H = rel("H"); // Here
	public static final Direction F = rel("F");
	public static final Direction B = rel("B");
	public static final Direction L = rel("L");
	public static final Direction R = rel("R");

	private final String name;
	private final boolean absolute;
	private final int dx, dy;
	private final int angle;

	private Direction(String name, boolean absolute, int dx, int dy, int angle) {
		this.name = name;
		this.absolute = absolute;
		this.dx = dx;
		this.dy = dy;
		this.angle = angle;
		CANON.put(name, this);
	}

	private static Direction abs(String n, int dx, int dy, int angle) {
		return new Direction(n, true, dx, dy, angle);
	}

	private static Direction rel(String n) {
		return new Direction(n, false, 0, 0, 0);
	}

	/**
	 * @return the interned direction for {@code name}, or {@code null} if unknown.
	 */
	public static Direction canonical(String name) {
		return CANON.get(name);
	}

	/** @return the cardinal (N/S/E/W) of a screen-space delta; E at the origin. */
	public static Direction cardinal(double dx, double dy) {
		if (Math.abs(dx) >= Math.abs(dy))
			return dx >= 0 ? E : W;
		return dy < 0 ? N : S;
	}

	/** @return the cardinal (N/S/E/W) nearest to an orientation in degrees. */
	public static Direction ofAngle(int angle_degree) {
		int a = ((angle_degree % 360) + 360) % 360;
		a = (Math.round(a / 90.0f) * 90) % 360;
		switch (a) {
		case 90:
			return N;
		case 180:
			return W;
		case 270:
			return S;
		default:
			return E;
		}
	}

	/**
	 * The cardinal 180° from this one (only meaningful for absolute directions).
	 */
	public Direction opposite() {
		return cardinal(-dx, -dy);
	}

	/** The cardinal 90° clockwise (screen-space: E&rarr;S&rarr;W&rarr;N). */
	public Direction right() {
		return cardinal(-dy, dx);
	}

	/**
	 * The cardinal 90° counter-clockwise (screen-space: E&rarr;N&rarr;W&rarr;S).
	 */
	public Direction left() {
		return cardinal(dy, -dx);
	}

	/**
	 * Resolve this direction against an entity's {@code facing} (an absolute
	 * cardinal): absolute directions and {@code H} return themselves; the relatives
	 * {@code F}/{@code B}/{@code R}/{@code L} become the corresponding cardinal.
	 * This is how GAL's relative directions acquire meaning — a {@link Direction}
	 * constant cannot self-resolve because it does not know the entity.
	 */
	public Direction resolve(Direction facing) {
		if (absolute || this == H)
			return this;
		if (this == F)
			return facing;
		if (this == B)
			return facing.opposite();
		if (this == R)
			return facing.right();
		if (this == L)
			return facing.left();
		return this;
	}

	public boolean isAbsolute() {
		return absolute;
	}

	public boolean isRelative() {
		return !absolute;
	}

	public int dx() {
		return dx;
	}

	public int dy() {
		return dy;
	}

	public int toAngle() {
		return angle;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
