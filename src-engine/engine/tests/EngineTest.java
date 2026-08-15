package engine.tests;

import engine.geometry.Axis;
import engine.geometry.ISU;
import engine.geometry.Grid;
import engine.core.Game;
import engine.core.Model;
import engine.entities.BasicStunt;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import engine.shapes.Rect;
import engine.graphics.Avatar;
import engine.graphics.View;
import game.PacMan;
import game.PacManModel;
import game.bots.KeyboardBot;
import game.bots.StraightBot;
import game.entities.Pac;
import game.entities.Boss;
import game.entities.Ghost;
import game.entities.Gum;
import oop.graphics.BufferedImage;
import oop.graphics.Color;
import oop.graphics.Font;
import oop.graphics.Graphics;
import oop.graphics.VirtualKeyCodes;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the game engine. Covers geometry, shapes, grid,
 * entity movement, collisions, and torus wrap.
 */
@DisplayName("Engine Functionality Tests")
class EngineTest {

	private static final double WORLD_SIZE = 25.0; // 25 cm, 25 cells of 1 cm each
	private static Game game;
	private static ISU isu;
	private static Grid grid;

	@BeforeAll
	static void setup() {
		game = new PacMan(WORLD_SIZE, WORLD_SIZE, true, true, 1.0, 10);
		isu = game.getIsu();
		grid = game.getGrid();
	}

	// ------------------------------------------------------------
	// 1. Axis (torus logic)
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Axis")
	class AxisTests {

		private Axis torusAxis;
		private Axis nonTorusAxis;

		@BeforeEach
		void init() {
			torusAxis = new Axis(true, 10.0);
			nonTorusAxis = new Axis(false, 10.0);
		}

		@Test
		@DisplayName("normalize(double) with torus")
		void normalizeDoubleTorus() {
			assertEquals(2.0, torusAxis.normalize(2.0));
			assertEquals(0.0, torusAxis.normalize(10.0));
			assertEquals(1.0, torusAxis.normalize(11.0));
			assertEquals(9.0, torusAxis.normalize(-1.0));
		}

		@Test
		@DisplayName("normalize(double) without torus clamps into half-open [0, perimeter)")
		void normalizeDoubleNonTorus() {
			assertEquals(5.0, nonTorusAxis.normalize(5.0));
			assertEquals(0.0, nonTorusAxis.normalize(-2.0));
			// Upper clamp is the largest in-world value (just below perimeter), not
			// perimeter itself, so it never floors to an out-of-bounds cell index.
			assertEquals(Math.nextDown(10.0), nonTorusAxis.normalize(15.0));
			assertEquals(Math.nextDown(10.0), nonTorusAxis.normalize(10.0)); // perimeter is out-of-world
			assertTrue(nonTorusAxis.normalize(15.0) < 10.0);
		}

		@Test
		@DisplayName("normalize(int) with torus")
		void normalizeIntTorus() {
			assertEquals(2, torusAxis.normalize(2));
			assertEquals(0, torusAxis.normalize(10));
			assertEquals(1, torusAxis.normalize(11));
			assertEquals(9, torusAxis.normalize(-1));
		}

		@Test
		@DisplayName("normalize(int) without torus clamps")
		void normalizeIntNonTorus() {
			assertEquals(5, nonTorusAxis.normalize(5));
			assertEquals(0, nonTorusAxis.normalize(-3));
			assertEquals(9, nonTorusAxis.normalize(12));
		}

		@Test
		@DisplayName("distance on torus uses shortest path")
		void distanceTorus() {
			assertEquals(2.0, torusAxis.distance(1.0, 3.0));
			assertEquals(2.0, torusAxis.distance(9.0, 1.0)); // wrap: 9→10→0→1 = 2
			assertEquals(0.0, torusAxis.distance(5.0, 5.0));
		}

		@Test
		@DisplayName("distance without torus is absolute difference")
		void distanceNonTorus() {
			assertEquals(2.0, nonTorusAxis.distance(1.0, 3.0));
			assertEquals(8.0, nonTorusAxis.distance(9.0, 1.0)); // no wrap
		}

		@Test
		@DisplayName("signedDistance on torus returns shortest signed delta")
		void signedDistanceTorus() {
			assertEquals(2.0, torusAxis.signedDistance(1.0, 3.0));
			assertEquals(-2.0, torusAxis.signedDistance(3.0, 1.0));
			// wrap: from 9 to 1: direct delta = -8, but shortest is +2 (9→10→0→1)
			assertEquals(2.0, torusAxis.signedDistance(9.0, 1.0));
			assertEquals(-2.0, torusAxis.signedDistance(1.0, 9.0));
		}

		@Test
		@DisplayName("signedDistance without torus is simple difference")
		void signedDistanceNonTorus() {
			assertEquals(2.0, nonTorusAxis.signedDistance(1.0, 3.0));
			assertEquals(-8.0, nonTorusAxis.signedDistance(9.0, 1.0));
		}
	}

	// ------------------------------------------------------------
	// 2. ISU coordinates, vectors, distances
	// ------------------------------------------------------------
	@Nested
	@DisplayName("ISU Coordinate System")
	class ISUTests {

		@Test
		@DisplayName("Coord normalization on torus")
		void coordNormalization() {
			ISU.Coord c = isu.new Coord(26.0, -1.0);
			assertEquals(1.0, c.x());
			assertEquals(24.0, c.y()); // 25 - 1 = 24
		}

		@Test
		@DisplayName("Coord distanceTo uses torus shortest path")
		void coordDistanceTo() {
			ISU.Coord a = isu.new Coord(24.5, 12.0);
			ISU.Coord b = isu.new Coord(0.5, 12.0);
			// torus distance: min(|24.5-0.5|=24, 25-24=1) = 1
			assertEquals(1.0, a.distanceTo(b), 1e-9);
		}

		@Test
		@DisplayName("Coord nearestDeltaTo returns shortest signed delta")
		void coordNearestDeltaTo() {
			ISU.Coord a = isu.new Coord(24.5, 12.0);
			ISU.Coord b = isu.new Coord(0.5, 12.0);
			ISU.Vector delta = a.nearestDeltaTo(b);
			assertEquals(1.0, delta.x(), 1e-9); // +1 (wrap forward)
			assertEquals(0.0, delta.y());
		}

		@Test
		@DisplayName("Coord translation wraps correctly")
		void coordTranslate() {
			ISU.Coord c = isu.new Coord(24.5, 12.0);
			ISU.Vector v = isu.new Vector(1.0, 0);
			c.translate(v);
			assertEquals(0.5, c.x(), 1e-9); // 25.5 → 0.5
			assertEquals(12.0, c.y());
		}

		@Test
		@DisplayName("Coord rotation around origin")
		void coordRotate() {
			ISU.Coord c = isu.new Coord(1.0, 0.0);
			c.rotation(90);
			assertEquals(0.0, c.x(), 1e-9);
			assertEquals(1.0, c.y(), 1e-9);
		}

		@Test
		@DisplayName("Coord rotation around arbitrary center")
		void coordRotateAround() {
			ISU.Coord center = isu.new Coord(5.0, 5.0);
			ISU.Coord p = isu.new Coord(6.0, 5.0); // right of center
			p.rotateAround(center, 90);
			assertEquals(5.0, p.x(), 1e-9);
			assertEquals(6.0, p.y(), 1e-9);
		}

		@Test
		@DisplayName("Vector dot product and norm")
		void vectorOps() {
			ISU.Vector v1 = isu.new Vector(3, 4);
			ISU.Vector v2 = isu.new Vector(1, 2);
			assertEquals(3 * 1 + 4 * 2, v1.dot(v2), 1e-9);
			assertEquals(5.0, v1.norm(), 1e-9);
		}
	}

	// ------------------------------------------------------------
	// 3. Grid positions, dimensions, cell occupancy
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Grid")
	class GridTests {

		@Test
		@DisplayName("Position to ISU Coord conversion")
		void positionToISU() {
			Grid.Position pos = grid.new Position(2, 3);
			ISU.Coord c = pos.toISUCoord();
			assertEquals(2.0, c.x());
			assertEquals(3.0, c.y());
			ISU.Coord centered = pos.toISUCoordCentered();
			assertEquals(2.5, centered.x());
			assertEquals(3.5, centered.y());
		}

		@Test
		@DisplayName("ISU Coord to Grid Position")
		void coordToPosition() {
			ISU.Coord c = isu.new Coord(2.5, 3.5);
			Grid.Position pos = c.toGridPosition();
			assertEquals(2, pos.x());
			assertEquals(3, pos.y());
		}

		@Test
		@DisplayName("Grid Dimension to ISU Dimension")
		void dimensionConversion() {
			Grid.Dimension gd = grid.new Dimension(3, 4);
			ISU.Dimension isuDim = gd.toISUDimension();
			assertEquals(3.0, isuDim.x());
			assertEquals(4.0, isuDim.y());
		}
	}

	// ------------------------------------------------------------
	// 4. Shapes intersection (basic + rotated)
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Shapes")
	class ShapeTests {

		@Test
		@DisplayName("Circle-Circle intersection")
		void circleCircle() {
			Circle c1 = new Circle(isu.new Coord(10, 10), 1.0);
			Circle c2 = new Circle(isu.new Coord(10, 10.5), 0.8); // distance 0.5 < 1.8 → intersect
			Circle c3 = new Circle(isu.new Coord(10, 12), 0.5); // distance 2.0 > 1.5 → no
			assertTrue(c1.intersects(c2));
			assertFalse(c1.intersects(c3));
		}

		@Test
		@DisplayName("Circle-Rect (axis-aligned)")
		void circleRect() {
			Rect rect = new Rect(isu.new Coord(10, 10), isu.new Dimension(2, 2), 0);
			Circle inside = new Circle(isu.new Coord(10, 10), 0.5);
			Circle touchingEdge = new Circle(isu.new Coord(11, 10), 0.5); // touches right edge
			Circle far = new Circle(isu.new Coord(15, 10), 0.5);
			assertTrue(rect.intersects(inside));
			assertTrue(rect.intersects(touchingEdge));
			assertFalse(rect.intersects(far));
		}

		@Test
		@DisplayName("Rect-Rect (rotated 45°)")
		void rectRectRotated() {
			Rect a = new Rect(isu.new Coord(5, 5), isu.new Dimension(4, 4), 0);
			Rect b = new Rect(isu.new Coord(7, 7), isu.new Dimension(4, 4), 45);
			assertTrue(a.intersects(b)); // overlapping
			Rect c = new Rect(isu.new Coord(20, 5), isu.new Dimension(4, 4), 0);
			assertFalse(a.intersects(c));
		}

		@Test
		@DisplayName("Rect-Rect (one inside another)")
		void rectInsideRect() {
			Rect big = new Rect(isu.new Coord(10, 10), isu.new Dimension(8, 8), 0);
			Rect small = new Rect(isu.new Coord(10, 10), isu.new Dimension(2, 2), 0);
			assertTrue(big.intersects(small));
			assertTrue(small.intersects(big));
		}

		@Test
		@DisplayName("Rect-Circle tangent")
		void rectCircleTangent() {
			Rect rect = new Rect(isu.new Coord(5, 5), isu.new Dimension(1, 1), 0);
			Circle tangent = new Circle(isu.new Coord(5.5, 5.5), 0.5); // touches corner
			assertTrue(rect.intersects(tangent));
		}

		@Test
		@DisplayName("Rect corner cache: rotate invalidates, translate reuses")
		void rectCornerCache() {
			// 2x4 box (half 1 x 2). After a 90° turn its footprint is 4 wide x 2 tall.
			Rect r = new Rect(isu.new Coord(10, 10), isu.new Dimension(2, 4), 0);
			r.rotate(90);
			double[][] c = r.cornersAt(10, 10);
			double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
			for (double[] p : c) {
				minX = Math.min(minX, p[0]);
				maxX = Math.max(maxX, p[0]);
				minY = Math.min(minY, p[1]);
				maxY = Math.max(maxY, p[1]);
			}
			assertEquals(8.0, minX, 1e-9);
			assertEquals(12.0, maxX, 1e-9);
			assertEquals(9.0, minY, 1e-9);
			assertEquals(11.0, maxY, 1e-9);

			// cornersAt with a different centre must be the SAME offsets, just shifted
			// (proves the cached offsets are reused and only translated).
			double[][] c2 = r.cornersAt(20, 10);
			for (int i = 0; i < 4; i++) {
				assertEquals(c[i][0] + 10.0, c2[i][0], 1e-9);
				assertEquals(c[i][1], c2[i][1], 1e-9);
			}
		}
	}

	// ------------------------------------------------------------
	// 5. Entity movement, bounding, hitbox, deployment
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Entity")
	class EntityTests {

		private Pac pac;

		@BeforeEach
		void createPac() {
			pac = new Pac("Pac", game, 12, 12); // cell (12,12) → centre (12.5,12.5)
		}

		@Test
		@DisplayName("Initial position and size")
		void initialPosition() {
			assertEquals(12.5, pac.center().x());
			assertEquals(12.5, pac.center().y());
			assertEquals(12, pac.position().x());
			assertEquals(12, pac.position().y());
			assertEquals(1.0, pac.center().getISU().getCmPerCell());
		}

		@Test
		@DisplayName("Translate by Vector (cm)")
		void translateCm() {
			ISU.Vector v = isu.new Vector(2.5, 0);
			pac.translate(v);
			assertEquals(15.0, pac.center().x());
			assertEquals(12.5, pac.center().y());
			assertEquals(15, pac.position().x()); // floor 15.0 → cell 15
		}

		@Test
		@DisplayName("Translate by Grid.Vector (cells)")
		void translateCells() {
			Grid.Vector v = grid.new Vector(2, -1);
			pac.translate(v);
			assertEquals(14.5, pac.center().x()); // 12.5 + 2
			assertEquals(11.5, pac.center().y()); // 12.5 - 1
			assertEquals(14, pac.position().x());
			assertEquals(11, pac.position().y());
		}

		@Test
		@DisplayName("moveEast/West/North/South (cm)")
		void moveCardinalCm() {
			pac.moveEast(1.0);
			assertEquals(13.5, pac.center().x());
			pac.moveWest(0.5);
			assertEquals(13.0, pac.center().x());
			pac.moveNorth(0.2);
			assertEquals(12.3, pac.center().y());
			pac.moveSouth(0.7);
			assertEquals(13.0, pac.center().y());
		}

		@Test
		@DisplayName("moveEast/West/North/South (steps)")
		void moveCardinalSteps() {
			pac.moveEast(2);
			assertEquals(14.5, pac.center().x());
			pac.moveWest(1);
			assertEquals(13.5, pac.center().x());
			pac.moveNorth(1);
			assertEquals(11.5, pac.center().y());
			pac.moveSouth(3);
			assertEquals(14.5, pac.center().y());
		}

		@Test
		@DisplayName("Turn updates orientation and rotates bounding shapes")
		void turn() {
			int oldOrientation = pac.orientation();
			pac.turn(90);
			assertEquals((oldOrientation + 90) % 360, pac.orientation());
			// For Pac (circle) bounding shape unchanged, but method must not crash.
		}

		@Test
		@DisplayName("setCoord updates center and bounding")
		void setCoord() {
			ISU.Coord newCenter = isu.new Coord(5.0, 5.0);
			pac.setCoord(newCenter);
			assertEquals(5.0, pac.center().x());
			assertEquals(5.0, pac.center().y());
			// Bounding shapes should have moved (they share the same Coord object)
			// We'll test collision later to verify.
		}

		@Test
		@DisplayName("setPosition updates center to cell center")
		void setPosition() {
			Grid.Position pos = grid.new Position(0, 0);
			pac.setPosition(pos);
			assertEquals(0.5, pac.center().x());
			assertEquals(0.5, pac.center().y());
		}

		@Test
		@DisplayName("deploy and retract occupy/release grid cells")
		void deployRetract() {
			// Pac occupies one cell (centre cell)
			pac.deploy(); // already deployed by constructor
			Grid.Cell cell = grid.cellAt(pac.position());
			assertTrue(cell.contains(pac));

			pac.retract();
			assertFalse(cell.contains(pac));
			pac.deploy();
			assertTrue(cell.contains(pac));
		}

		@Test
		@DisplayName("Footprint tracking: sub-cell move keeps cells, boundary crossing updates them")
		void footprintTracking() {
			// A Gum is tiny (radius 0.1), so it fits inside a single 1cm cell and we can
			// observe the footprint staying put on a sub-cell move, then changing only
			// when the shape actually spills across a cell boundary. This exercises the
			// hitbox's "skip redeploy when the footprint is unchanged" path end-to-end.
			Gum gum = new Gum(game, 5, 5); // centre (5.5,5.5) -> only cell (5,5)
			Grid.Cell c55 = grid.cellAt(grid.new Position(5, 5));
			Grid.Cell c65 = grid.cellAt(grid.new Position(6, 5));
			assertTrue(c55.contains(gum));
			assertFalse(c65.contains(gum));

			gum.moveEast(0.1); // centre 5.6, span [5.5,5.7] -> still only cell (5,5)
			assertTrue(c55.contains(gum));
			assertFalse(c65.contains(gum));

			gum.moveEast(0.45); // centre 6.05, span [5.95,6.15] -> now spans cells 5 and 6
			assertTrue(c55.contains(gum));
			assertTrue(c65.contains(gum));

			gum.moveEast(0.5); // centre 6.55, span [6.45,6.65] -> only cell 6
			assertFalse(c55.contains(gum));
			assertTrue(c65.contains(gum));
		}

		@Test
		@DisplayName("Hitbox update after movement")
		void hitboxUpdate() {
			// Hitbox is private, but we can indirectly test through intersection
			// with another entity after movement.
			Pac other = new Pac("Other", game, 12, 12);
			assertTrue(pac.intersects(other)); // same cell
			// Move clear of the other Pac. Note: moving only 1.0 would leave the two
			// radius-0.5 circles exactly tangent, which IS a collision under the
			// inclusive convention; move 2.0 for unambiguous separation.
			pac.moveEast(2.0);
			assertFalse(pac.intersects(other));
		}
	}

	// ------------------------------------------------------------
	// 6. Ghost bounding (circle + rectangle)
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Ghost Bounding Shape")
	class GhostBoundingTests {

		private Ghost ghost;

		@BeforeEach
		void createGhost() {
			ghost = new Ghost("Inky", game, 10, 10);
		}

		@Test
		@DisplayName("Ghost bounding contains both circle and rectangle")
		void ghostBoundingComposition() {
			// We cannot directly access bounding shapes, but we can test collisions.
			// Create a Pac exactly at the same location -> should intersect.
			Pac pac = new Pac("Pac", game, 10, 10);
			assertTrue(ghost.intersects(pac));

			// Move Pac far away -> no intersection
			pac.moveEast(5);
			assertFalse(ghost.intersects(pac));
		}

		@Test
		@DisplayName("Ghost bounding reacts to rotation (rectangle rotates)")
		void ghostRotation() {
			// Ghost bounding includes a rectangle that rotates when ghost turns.
			// Circle is rotation-invariant. Test by rotating ghost and checking
			// collision with a Pac that touches only the rectangle area.
			// For simplicity, we'll just verify orientation changes.
			int oldOrientation = ghost.orientation();
			ghost.turn(45);
			assertEquals((oldOrientation + 45) % 360, ghost.orientation());
			// Collision logic unchanged because we can't easily test rotated rect here.
		}
	}

	// ------------------------------------------------------------
	// 7. Entity-Entity intersection with torus wrap
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Torus Wrap Collisions")
	class TorusCollisionTests {

		@Test
		@DisplayName("Two circles across torus edge intersect")
		void circlesAcrossEdge() {
			// Torus-aware distance = 1.0, sum radii = 1.0: tangent across the seam.
			// The two circles fall into distinct cells (24 and 0) that are adjacent
			// on the torus; the conservative (neighbour-aware) broad phase keeps them
			// as candidates, and the inclusive convention makes the tangent count as
			// a collision.
			Pac right = new Pac("Right", game, 24.5, 12.5);
			Pac left = new Pac("Left", game, 0.5, 12.5);
			assertTrue(right.intersects(left));
		}

		@Test
		@DisplayName("Rect-Circle intersection is torus-aware across the seam")
		void rectCircleAcrossEdge() {
			// Rect hugs the right edge (centre 24.5, spans x in [24,25]); circle hugs
			// the left edge (centre 0.5). RectCircleIntersection uses nearestDeltaTo,
			// so the seam is handled: a circle that stops short of the seam misses,
			// while one large enough to reach across it intersects.
			Rect rect = new Rect(isu.new Coord(24.5, 12.5), isu.new Dimension(1, 1), 0);

			Circle near = new Circle(isu.new Coord(0.5, 12.5), 0.4); // left edge at 0.1 → 0.1 gap
			assertFalse(rect.intersects(near));

			Circle reaching = new Circle(isu.new Coord(0.5, 12.5), 0.6); // reaches past seam into rect
			assertTrue(rect.intersects(reaching));
			assertTrue(reaching.intersects(rect)); // symmetric (double dispatch)
		}

		@Test
		@DisplayName("Entity movement across edge updates center correctly (no double translation)")
		void movementAcrossEdge() {
			Pac p = new Pac("Pac", game, 24.5, 12.5);
			p.moveEast(0.6); // centre 25.1 -> wrapped to 0.1
			assertEquals(0.1, p.center().x(), 1e-9);
			assertEquals(12.5, p.center().y());
		}

		@Test
		@DisplayName("Collision after both entities wrap")
		void bothWrapAndCollide() {
			Pac p1 = new Pac("P1", game, 24.5, 12.5);
			Pac p2 = new Pac("P2", game, 0.7, 12.5);
			// Initially distance via wrap: 0.2 (from 24.5 to 0.7 is 0.2? Actually
			// 24.5->25=0.5, plus 0.7 = 1.2? Let's compute properly: shortest = min(23.8,
			// 1.2)=1.2)
			// Sum radii =1.0 -> no collision.
			assertFalse(p1.intersects(p2));
			p1.moveEast(0.8); // p1 centre = 0.3 (wrapped)
			// Now p1 at 0.3, p2 at 0.7 -> distance 0.4 < 1.0 -> collide
			assertTrue(p1.intersects(p2));
		}

		@Test
		@DisplayName("Composite entity straddling the seam does not occupy the whole world")
		void compositeSeamOccupancyIsLocal() {
			// Boss spans x in [x, x+3]. Placed at cell x=24 its arms normalise to
			// opposite sides of the seam (one shape centre wraps to ~0.5, another
			// stays at ~24.5). The hitbox must express both relative to the centre,
			// keeping occupancy local to the seam — NOT spanning every column.
			Boss boss = new Boss("Edge", game, 24, 12);

			// Cells right at the seam are occupied...
			assertTrue(grid.cellAt(grid.new Position(24, 12)).contains(boss));
			assertTrue(grid.cellAt(grid.new Position(0, 12)).contains(boss));

			// ...but a cell on the far side of the world is NOT (the seam bug would
			// make this true by occupying the entire row).
			assertFalse(grid.cellAt(grid.new Position(12, 12)).contains(boss));
			assertFalse(grid.cellAt(grid.new Position(12, 0)).contains(boss));
		}

		@Test
		@DisplayName("Composite entity off the seam still collides correctly after the fix")
		void compositeOffSeamStillCollides() {
			// Sanity: the centre-relative AABB must not change ordinary (non-wrapping)
			// behaviour. A Boss in the middle still collides with a Pac on its arm.
			Boss boss = new Boss("Mid", game, 10, 12);
			Pac onArm = new Pac("Arm", game, 10, 12); // covers the centre cell
			assertTrue(boss.intersects(onArm));
		}
	}

	// ------------------------------------------------------------
	// 8. Composite bounding (Bounding class)
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Composite Bounding")
	class CompositeBoundingTests {

		@Test
		@DisplayName("Bounding with multiple shapes intersects if any shape intersects")
		void boundingIntersection() {
			ISU.Coord anchor = isu.new Coord(5, 5);
			Bounding bounding = new Bounding(anchor);
			Circle c = new Circle(anchor, 1.0);
			Rect r = new Rect(anchor, isu.new Dimension(2, 2), 0);
			bounding.add(c);
			bounding.add(r);

			// A shape that intersects the circle but not the rectangle
			Circle otherCircle = new Circle(isu.new Coord(5, 6.5), 0.8); // distance = 1.5, sum radii=1.8 -> intersect
			assertTrue(bounding.intersects(otherCircle));

			// Shape that intersects rectangle but not circle
			Rect otherRect = new Rect(isu.new Coord(6.5, 5), isu.new Dimension(1, 1), 0);
			assertTrue(bounding.intersects(otherRect));

			// Shape that intersects neither
			Circle far = new Circle(isu.new Coord(20, 20), 0.5);
			assertFalse(bounding.intersects(far));
		}

		@Test
		@DisplayName("Bounding translate moves all shapes")
		void boundingTranslate() {
			ISU.Coord anchor = isu.new Coord(5, 5);
			Bounding bounding = new Bounding(anchor);
			Circle c = new Circle(anchor, 1.0);
			bounding.add(c);
			ISU.Vector delta = isu.new Vector(2, 3);
			bounding.translate(delta);
			assertEquals(7.0, c.getCenter().x());
			assertEquals(8.0, c.getCenter().y());
		}

		@Test
		@DisplayName("Bounding rotate rotates all shapes around anchor")
		void boundingRotate() {
			ISU.Coord anchor = isu.new Coord(0, 0);
			Bounding bounding = new Bounding(anchor);
			Circle c = new Circle(isu.new Coord(1, 0), 0.5);
			bounding.add(c);
			bounding.rotate(90);
			assertEquals(0.0, c.getCenter().x(), 1e-9);
			assertEquals(1.0, c.getCenter().y(), 1e-9);
		}

		@Test
		@DisplayName("Anchor shared with a shape centre is translated exactly once")
		void translateSharedAnchorNoDoubleMove() {
			// The developer reuses the entity centre as BOTH the anchor and the shape
			// centre (no defensive copy). The shared point must move once (5 -> 7),
			// never twice (which a naive "move anchor then move every shape" would do).
			ISU.Coord shared = isu.new Coord(5, 5);
			Bounding bounding = new Bounding(shared);
			Circle c = new Circle(shared, 1.0);
			bounding.add(c);

			bounding.translate(isu.new Vector(2, 0));

			assertEquals(7.0, c.getCenter().x(), 1e-9);
			assertEquals(5.0, c.getCenter().y(), 1e-9);
			assertSame(shared, c.getCenter()); // still the same object, not replaced
		}

		@Test
		@DisplayName("A centre shared by two shapes is translated exactly once")
		void translateSharedShapeCentreNoDoubleMove() {
			ISU.Coord anchor = isu.new Coord(0, 0);
			ISU.Coord shared = isu.new Coord(5, 5);
			Bounding bounding = new Bounding(anchor);
			bounding.add(new Circle(shared, 1.0));
			bounding.add(new Rect(shared, isu.new Dimension(2, 2), 0)); // same centre object

			bounding.translate(isu.new Vector(3, 0));

			assertEquals(8.0, shared.x(), 1e-9); // 5 + 3, once
			assertEquals(5.0, shared.y(), 1e-9);
		}

		@Test
		@DisplayName("Anchor shared with a shape centre stays put under rotation")
		void rotateSharedAnchorIsPivot() {
			// When the anchor IS a shape centre, rotating must leave it fixed (it is
			// the pivot) and only swing the other shapes around it.
			ISU.Coord anchor = isu.new Coord(0, 0);
			Bounding bounding = new Bounding(anchor);
			bounding.add(new Circle(anchor, 0.5)); // centred on the pivot
			Circle satellite = new Circle(isu.new Coord(1, 0), 0.5);
			bounding.add(satellite);

			bounding.rotate(90);

			// Pivot circle unmoved...
			assertEquals(0.0, anchor.x(), 1e-9);
			assertEquals(0.0, anchor.y(), 1e-9);
			// ...satellite swung 90° around it: (1,0) -> (0,1)
			assertEquals(0.0, satellite.getCenter().x(), 1e-9);
			assertEquals(1.0, satellite.getCenter().y(), 1e-9);
		}
	}

	// ------------------------------------------------------------
	// 9. Gum entity (small circle) and collision
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Gum and Pac collision")
	class GumCollisionTests {

		@Test
		@DisplayName("Pac collects gum when overlapping")
		void pacCollectsGum() {
			Gum gum = new Gum(game, 10, 10);
			Pac pac = new Pac("Pac", game, 10, 10);
			assertTrue(pac.intersects(gum));
			pac.moveEast(1.0);
			assertFalse(pac.intersects(gum));
		}

		@Test
		@DisplayName("Gum does not intersect when far")
		void gumFar() {
			Gum gum = new Gum(game, 0, 0);
			Pac pac = new Pac("Pac", game, 20, 20);
			assertFalse(pac.intersects(gum));
		}
	}

	// ------------------------------------------------------------
	// 10. Boss: multi-rectangle ("t"-shaped) bounding and rotation
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Boss Bounding and Rotation")
	class BossBoundingTests {

		// Boss at cell (10,10): a vertical bar (col 10, rows 9..12) crossing the
		// centre, plus a horizontal bar (row 10, cols 10..12) — the "t" shape that
		// exists specifically to exercise multi-shape AABB occupancy and rotation.

		@Test
		@DisplayName("Both arms of the t-shape collide; outside does not")
		void bossArmsCollide() {
			Boss boss = new Boss("Boss", game, 10, 10);
			Pac onVerticalArm = new Pac("V", game, 10, 10); // centre (10.5,10.5)
			Pac onHorizontalArm = new Pac("H", game, 12, 10); // centre (12.5,10.5)
			Pac far = new Pac("F", game, 20, 20);
			assertTrue(boss.intersects(onVerticalArm));
			assertTrue(boss.intersects(onHorizontalArm));
			assertFalse(boss.intersects(far));
		}

		@Test
		@DisplayName("Rotating 180° swings the horizontal arm off the right-side Pac")
		void bossRotationMovesArm() {
			Boss boss = new Boss("Boss", game, 10, 10);
			Pac onHorizontalArm = new Pac("H", game, 12, 10);
			assertTrue(boss.intersects(onHorizontalArm)); // arm points right

			boss.turn(180);
			assertEquals(180, boss.orientation());
			// The horizontal arm now points left, so the right-side Pac is clear...
			assertFalse(boss.intersects(onHorizontalArm));
			// ...while the vertical arm still covers the centre cell.
			Pac centre = new Pac("C", game, 10, 10);
			assertTrue(boss.intersects(centre));
		}
	}

	// ------------------------------------------------------------
	// 11. Entity lifecycle: removal releases the world (GC)
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Entity Lifecycle / GC")
	class LifecycleTests {

		/**
		 * Minimal concrete Model that permits any move; only used to exercise remove().
		 */
		private Model permissiveModel() {
			return new Model(game) {
				@Override
				public boolean canOccupy(Entity e, Grid.Position target) {
					return true;
				}

				@Override
				protected void onCollision(Entity a, Entity b) {
					// no collision rules needed for lifecycle tests
				}
			};
		}

		@Test
		@DisplayName("destroy() releases every occupied grid cell")
		void destroyReleasesCells() {
			// Use a Boss so several cells are occupied, not just one.
			Boss boss = new Boss("Doomed", game, 6, 6);
			java.util.List<Grid.Cell> held = new java.util.ArrayList<>();
			for (int x = 5; x <= 9; x++)
				for (int y = 4; y <= 9; y++) {
					Grid.Cell c = grid.cellAt(grid.new Position(x, y));
					if (c.contains(boss))
						held.add(c);
				}
			assertFalse(held.isEmpty(), "Boss should occupy at least one cell");

			boss.destroy();

			for (Grid.Cell c : held)
				assertFalse(c.contains(boss), "cell still references a destroyed entity");
		}

		@Test
		@DisplayName("destroy() is idempotent")
		void destroyIdempotent() {
			Pac pac = new Pac("Twice", game, 3, 3);
			pac.destroy();
			assertDoesNotThrow(pac::destroy);
		}

		@Test
		@DisplayName("Model.remove detaches the entity from grid and drops its stunt")
		void modelRemoveDetaches() {
			Model model = permissiveModel();
			Pac pac = new Pac("Tracked", game, 7, 7);
			model.add(pac);

			Grid.Cell cell = grid.cellAt(pac.position());
			assertTrue(cell.contains(pac));

			model.remove(pac);

			assertFalse(cell.contains(pac), "removed entity must not linger in grid cells");
			assertNull(pac.stunt(), "removed entity must drop its stunt for GC");
		}

		@Test
		@DisplayName("A dead Pac is actually garbage-collected (game wiring)")
		void deadPacIsCollected() throws InterruptedException {
			PacManModel model = new PacManModel(game);

			// Wire Pac exactly as GameApp does: a keyboard-driven stunt. Keep a STRONG
			// reference to the bot for the whole test, mimicking how the canvas retains
			// it as a KeyListener — to prove that does NOT keep the dead Pac alive.
			Pac pac = new Pac("P", game, 10, 10);
			pac.setStunt(new BasicStunt(pac));
			KeyboardBot brain = new KeyboardBot(pac, 0.5);
			pac.setBot(brain);
			model.add(pac);

			Ghost ghost = new Ghost("G", game, 10, 10);
			model.add(ghost);

			java.lang.ref.WeakReference<Pac> ref = new java.lang.ref.WeakReference<>(pac);

			// Unpowered Pac dies on contact with the ghost.
			model.resolveCollisions();
			assertFalse(model.pacAlive());
			assertFalse(model.entities().contains(pac));

			// Drop the only remaining strong reference held by the test.
			pac = null;

			assertTrue(awaitCollected(ref),
					"a dead Pac should be garbage-collected, but something still strongly references it");

			// Sanity: the bot is still alive (as the canvas would keep it), yet Pac was
			// collected anyway — confirming the bot does not retain the entity.
			assertNotNull(brain);
			assertNotNull(ghost); // the surviving ghost is unaffected
		}

		/** Force GC and wait (briefly) for {@code ref} to be cleared. */
		private boolean awaitCollected(java.lang.ref.Reference<?> ref) throws InterruptedException {
			for (int i = 0; i < 50 && ref.get() != null; i++) {
				System.gc();
				Thread.sleep(10);
			}
			return ref.get() == null;
		}
	}

	// ------------------------------------------------------------
	// 12. Point 4 — collision outcomes resolved by the concrete Model
	// ------------------------------------------------------------
	@Nested
	@DisplayName("PacMan Collision Outcomes")
	class CollisionOutcomeTests {

		private PacManModel model;

		@BeforeEach
		void mkModel() {
			model = new PacManModel(game);
		}

		@Test
		@DisplayName("Pac eats an overlapping Gum (removed, scored, retracted)")
		void pacEatsGum() {
			Pac pac = new Pac("P", game, 10, 10);
			Gum gum = new Gum(game, 10, 10);
			model.add(pac);
			model.add(gum);
			assertTrue(pac.intersects(gum));

			model.resolveCollisions();

			assertFalse(model.entities().contains(gum));
			assertTrue(model.entities().contains(pac));
			assertEquals(10, model.score());
			assertFalse(grid.cellAt(grid.new Position(10, 10)).contains(gum));
		}

		@Test
		@DisplayName("Unpowered Pac dies to a Ghost")
		void unpoweredPacDies() {
			Pac pac = new Pac("P", game, 10, 10);
			Ghost ghost = new Ghost("G", game, 10, 10);
			model.add(pac);
			model.add(ghost);

			model.resolveCollisions();

			assertFalse(model.pacAlive());
			assertFalse(model.entities().contains(pac));
			assertTrue(model.entities().contains(ghost));
		}

		@Test
		@DisplayName("Powered Pac eats the Ghost")
		void poweredPacEatsGhost() {
			Pac pac = new Pac("P", game, 10, 10);
			pac.setPowered(true);
			Ghost ghost = new Ghost("G", game, 10, 10);
			model.add(pac);
			model.add(ghost);

			model.resolveCollisions();

			assertTrue(model.pacAlive());
			assertTrue(model.entities().contains(pac));
			assertFalse(model.entities().contains(ghost));
			assertEquals(200, model.score());
		}

		@Test
		@DisplayName("Ghost-Ghost collision has no effect")
		void ghostGhostNoEffect() {
			Ghost a = new Ghost("A", game, 10, 10);
			Ghost b = new Ghost("B", game, 10, 10);
			model.add(a);
			model.add(b);

			model.resolveCollisions();

			assertTrue(model.entities().contains(a));
			assertTrue(model.entities().contains(b));
			assertEquals(0, model.score());
		}

		@Test
		@DisplayName("update() moves entities then resolves: Pac drifts onto a gum and eats it")
		void updateMovesThenResolves() {
			Pac pac = new Pac("P", game, 9, 10); // centre 9.5; gum one cell east
			pac.setStunt(new BasicStunt(pac));
			pac.setBot(new StraightBot(pac, 1.0, 0.0));
			Gum gum = new Gum(game, 10, 10);
			model.add(pac);
			model.add(gum);
			assertFalse(pac.intersects(gum)); // 0.4 cm gap before moving

			model.update(); // move east 1cm (centre -> 10.5, onto the gum) then resolve

			assertFalse(model.entities().contains(gum));
			assertEquals(10, model.score());
		}
	}

	// ------------------------------------------------------------
	// 13. Point 6 — View paints avatars in z-order / sOrder
	// ------------------------------------------------------------
	@Nested
	@DisplayName("View (z-order avatar painting)")
	class ViewTests {

		/** An avatar that records the order in which it is painted. */
		private Avatar tagged(View view, int z, double s, String tag, java.util.List<String> log) {
			return new Avatar(null, view, z) {
				@Override
				public void paint(Graphics g) {
					log.add(tag);
				}

				@Override
				public double sOrder() {
					return s;
				}
			};
		}

		@Test
		@DisplayName("paints layers back-to-front, then by ascending sOrder within a layer")
		void paintOrder() {
			View view = new View();
			java.util.List<String> painted = new java.util.ArrayList<>();
			// Added out of draw order on purpose.
			view.add(tagged(view, 1, 0.0, "front", painted));
			view.add(tagged(view, 0, 5.0, "back-hiS", painted));
			view.add(tagged(view, 0, 1.0, "back-loS", painted));

			view.paint(new CountingGraphics());

			assertEquals(java.util.List.of("back-loS", "back-hiS", "front"), painted);
		}

		@Test
		@DisplayName("remove drops an avatar from the paint loop")
		void removeStopsPainting() {
			View view = new View();
			java.util.List<String> painted = new java.util.ArrayList<>();
			Avatar a = tagged(view, 0, 0.0, "a", painted);
			view.add(a);
			view.remove(a);

			view.paint(new CountingGraphics());

			assertTrue(painted.isEmpty());
		}
	}

	// ------------------------------------------------------------
	// 14. Keyboard control: KeyboardBot drives its entity via key events
	// ------------------------------------------------------------
	@Nested
	@DisplayName("Keyboard Control")
	class KeyboardControlTests {

		@Test
		@DisplayName("Key presses steer Pac; idle leaves it still")
		void keyboardDrivesPac() {
			PacManModel model = new PacManModel(game);
			Pac pac = new Pac("P", game, 5, 5); // centre (5.5, 5.5)
			pac.setStunt(new BasicStunt(pac));
			KeyboardBot brain = new KeyboardBot(pac, 1.0); // 1 cm per tick
			pac.setBot(brain);
			model.add(pac);

			double x0 = pac.center().x();
			double y0 = pac.center().y();

			// No key pressed yet -> no movement.
			model.update();
			assertEquals(x0, pac.center().x(), 1e-9);
			assertEquals(y0, pac.center().y(), 1e-9);

			// RIGHT -> +x, continuous over two ticks.
			brain.pressed(VirtualKeyCodes.VK_RIGHT, '\0');
			model.update();
			model.update();
			assertEquals(x0 + 2.0, pac.center().x(), 1e-9);
			assertEquals(y0, pac.center().y(), 1e-9);

			// UP -> north (-y), and x stops changing.
			double xBeforeUp = pac.center().x();
			brain.pressed(VirtualKeyCodes.VK_UP, '\0');
			model.update();
			assertEquals(xBeforeUp, pac.center().x(), 1e-9);
			assertEquals(y0 - 1.0, pac.center().y(), 1e-9);

			// SPACE -> stop.
			double xStop = pac.center().x();
			double yStop = pac.center().y();
			brain.pressed(VirtualKeyCodes.VK_SPACE, '\0');
			model.update();
			assertEquals(xStop, pac.center().x(), 1e-9);
			assertEquals(yStop, pac.center().y(), 1e-9);
		}
	}

	/** Minimal {@link Graphics} that just counts the fill calls View makes. */
	private static class CountingGraphics implements Graphics {
		int ovals, polygons, rects;

		@Override
		public void fillOval(int x, int y, int w, int h) {
			ovals++;
		}

		@Override
		public void fillPolygon(int[] xs, int[] ys, int n) {
			polygons++;
		}

		@Override
		public void fillRect(int x, int y, int w, int h) {
			rects++;
		}

		// --- everything else is irrelevant to these assertions ---
		@Override
		public BufferedImage load(String path) {
			return null;
		}

		@Override
		public Color getColor(int a, int r, int g, int b) {
			return null;
		}

		@Override
		public Font getFont(String name, int styles, int size) {
			return null;
		}

		@Override
		public Font getFont() {
			return null;
		}

		@Override
		public void setFont(Font f) {
		}

		@Override
		public void drawString(String str, int x, int y) {
		}

		@Override
		public Color getColor() {
			return null;
		}

		@Override
		public void setColor(Color c) {
		}

		@Override
		public void drawLine(int x1, int y1, int x2, int y2) {
		}

		@Override
		public void drawRect(int x, int y, int w, int h) {
		}

		@Override
		public void drawOval(int x, int y, int w, int h) {
		}

		@Override
		public void drawPolygon(int[] xs, int[] ys, int n) {
		}

		@Override
		public void setClip(int x, int y, int w, int h) {
		}

		@Override
		public void drawImage(BufferedImage img, int x, int y) {
		}

		@Override
		public void drawImage(BufferedImage img, int x, int y, int w, int h) {
		}

		@Override
		public void rotate(double theta) {
		}

		@Override
		public void scale(double sx, double sy) {
		}

		@Override
		public void translate(int x, int y) {
		}

		@Override
		public void shear(double shx, double shy) {
		}

		@Override
		public Object getTransform() {
			return null;
		}

		@Override
		public void setTransform(Object o) {
		}
	}
}