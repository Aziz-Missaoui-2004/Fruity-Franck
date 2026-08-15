package engine.geometry;

// = AXIS =

/**
 * @apiNote Axis of a Torus with origin at 0
 * @implNote Coordinate ranges in [0 ; perimeter[
 * @implNote Negative coordinate are allowed
 */

public class Axis {

	// FIELDS

	boolean onTorus;
	double perimeter;
	double halfPerimeter;

	// CONSTRUCTOR

	public Axis(boolean onTorus, double perimeter) {
		this.onTorus = onTorus;
		this.perimeter = perimeter;
		halfPerimeter = perimeter / 2;
	}

	// NORMALIZE INTEGER LENGTH

	/**
	 * @apiNote normalize _integer length_ according to the geometry
	 * @implNote returns positive values
	 * @return
	 *         <UL>
	 *         <LI>length % perimeter __&in; [0, perimeter-1]__ if onTorus</LI>
	 *         <LI>length if !onTorus</LI>
	 *         </UL>
	 */
	public int normalize(int length) {
		if (!onTorus) {
			if (length >= (int) perimeter)
				return (int) perimeter - 1;
			if (length < 0)
				return 0;
			return length;
		}
		return modp(length, (int) perimeter);
	}

	/**
	 * @apiNote compute length modulo perimeter
	 * @return length % perimeter __&in; [0, perimeter-1]__
	 */
	private int modp(int length, int perimeter) {
		return Math.floorMod(length, perimeter);
	}

	// NORMALIZE REAL LENGTH

	/**
	 * @apiNote normalize _real length_ according to the geometry
	 * @return
	 *         <UL>
	 *         <LI>length modulo perimeter <I>&in; [0 , perimeter[</I> if
	 *         onTorus</LI>
	 *         <LI>length clamped into <I>[0 , perimeter[</I> if !onTorus</LI>
	 *         </UL>
	 */
	public double normalize(double length) {
		if (!onTorus) {
			if (length >= perimeter)
				return Math.nextDown(perimeter);
			else if (length < 0)
				return 0;
			return length;
		}

		return modp(length, perimeter);
	}

	/**
	 * @apiNote compute length modulo perimeter
	 * @return length % perimeter __&in; [0, perimeter[__
	 */
	private double modp(double length, double perimeter) {
		double result = length % perimeter;
		if (result < 0) {
			result += perimeter;
		}
		return result;
	}

	// DISTANCE

	/**
	 * @apiNote The distance on a Torus is that of the shortest path, sometimes
	 *          going in the opposite direction and across the border is shorter.
	 * @implNote Look for the detail on internet.
	 * @implNote assumes normalized positions
	 */
	public double distance(double position1, double position2) {
		double direct = Math.abs(position1 - position2);
		if (onTorus) {
			double warp = perimeter - direct;
			return Math.min(warp, direct);
		}

		return direct;
	}

	/**
	 * @impleNote Returns the shortus in a torus or a euclidian world
	 * @impleNote Can return a negative distance.
	 */
	public double signedDistance(double from, double to) {
		if (!onTorus)
			return to - from;

		double delta = to - from;
		if (delta > halfPerimeter)
			delta -= perimeter;
		else if (delta < -halfPerimeter)
			delta += perimeter;
		return delta;
	}
}
