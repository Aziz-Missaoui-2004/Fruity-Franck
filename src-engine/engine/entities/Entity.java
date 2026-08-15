package engine.entities;

import java.io.PrintStream;

import engine.core.Game;
import engine.core.Model;
import engine.geometry.Grid;
import engine.geometry.Grid.Cell;
import engine.geometry.ISU;
import engine.graphics.Avatar;

// == ENTITY ==

public abstract class Entity {

	// FIELDS

	protected final Grid grid;
	protected ISU isu;
	String name;

	// FIELDS

	protected ISU.Dimension size; // dimension de l'entité
	ISU.Vector step; // dimension d'un pas de déplacement
	protected Grid.Position position; // position dans la grille
	protected ISU.Coord center; // coordonnées en cm du centre de l'entité

	// FIELDS

	int orientation_degree; // orientation par rapport à l'axe des x
	private Bounding bounding;
	private Hitbox hitbox;
	private Stunt stunt; // actuator (executes motion); null until attached
	private Bot bot; // decision-maker (woken by the Brain); null for inert props
	private Model model; // the world this entity lives in; set on Model.add

	// Command fields written by the AI/bot task (via the stunt's drive) and read by
	// the motion task.
	private ISU.Vector linearSpeed;
	private double angularSpeed_degree;

	private Avatar avatar;

	// CONSTRUCTOR

	protected Entity(String name, Game game) {
		this.name = name;
		this.grid = game.getGrid();
		this.isu = game.getIsu();
		this.setHitbox(new Hitbox(this, isu, grid));
	}

	// SETTER

	public void setPosition(Grid.Position position) {
		ISU.Coord newCenter = position.toISUCoordCentered();
		setCoord(newCenter);
	}

	public void setCoord(ISU.Coord newCenter) {
		if (center != null && bounding != null) {
			ISU.Vector delta = isu.new Vector(newCenter.x() - center.x(), newCenter.y() - center.y());
			bounding.translate(delta);
		}
		this.center = newCenter.mkCopy();
		this.position = center.toGridPosition();
		updateHitbox();
	}

	public void setSize(Grid.Dimension dimension) {
		this.size = dimension.toISUDimension();
	}

	public void setSize(ISU.Dimension dimension) {
		this.size = dimension;
	}

	public void setStep(ISU.Vector step) {
		this.step = step;
	}

	public void setStunt(Stunt stunt) {
		this.stunt = stunt;
	}

	public void setBot(Bot bot) {
		this.bot = bot;
	}

	public void setAngularSpeed_degree(double angularSpeed_degree) {
		this.angularSpeed_degree = angularSpeed_degree;
	}

	public void setLinearSpeed(ISU.Vector linearSpeed) {
		this.linearSpeed = linearSpeed;
	}

	public void setAvatar(Avatar avatar) {
		this.avatar = avatar;
	}

	// GETTER

	public ISU.Coord center() {
		return center;
	}

	public Grid.Position position() {
		return position;
	}

	public int orientation() {
		return orientation_degree;
	}

	public Stunt stunt() {
		return stunt;
	}

	public double getAngularSpeed_degree() {
		return angularSpeed_degree;
	}

	public ISU.Vector getLinearSpeed() {
		return linearSpeed;
	}

	public Avatar getAvatar() {
		return avatar;
	}

	/** @return the decision-maker driving this entity, or {@code null} if inert. */
	public Bot bot() {
		return bot;
	}

	/** @return the world this entity belongs to, or {@code null} if unmanaged. */
	public Model model() {
		return model;
	}

	/**
	 * Set by {@link engine.core.Model#add(Entity)} so the entity can reach its
	 * world.
	 */
	public void setModel(engine.core.Model model) {
		this.model = model;
	}

	/**
	 * @return the bounding (collision/draw shapes) of this entity, or {@code null}
	 *         if unset.
	 */
	public Bounding bounding() {
		return bounding;
	}

	/** @return this entity's display name. */
	public String name() {
		return name;
	}

	// TRANSLATION

	public void translate(ISU.Vector v) {
		this.center.translate(v);
		this.position = center.toGridPosition();
		if (bounding != null)
			bounding.translate(v);
		updateHitbox();
	}

	public void translate(Grid.Vector v) {
		ISU.Vector cmV = isu.new Vector(v.x() * isu.getCmPerCell(), v.y() * isu.getCmPerCell());
		translate(cmV);
	}

	// TURN

	/**
	 * @apiNote turn is a rotation around the center of the entity.
	 * @param angle_degree
	 */
	public void turn(int angle_degree) {
		orientation_degree = (orientation_degree + angle_degree) % 360;
		if (orientation_degree < 0)
			orientation_degree += 360;

		if (bounding != null)
			bounding.rotate(angle_degree);

		updateHitbox();
	}

	/**
	 * Reorient the entity to face {@code targetOrientation_degree} absolutely
	 * (turning by the necessary delta, so bounding shapes rotate consistently).
	 * Used by the GAL {@code Move}/{@code Turn} actions so an entity's facing
	 * tracks the direction it heads, which is what relative GAL directions resolve
	 * against.
	 */
	public void face(int targetOrientation_degree) {
		turn(targetOrientation_degree - orientation_degree);
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.println("Entity \"" + name + "\"");
		ps.print("  Position: ");
		position.show(ps);
		ps.println();
		ps.print("  Center: ");
		center.show(ps);
		ps.println();
		ps.print("Occupied cells : ");
		for (Cell c : getHitbox().occupiedCells())
			c.show(ps);
		ps.println();
		ps.println("  Orientation: " + orientation_degree + "°");
		ps.print("  Size: ");
		size.show(ps);
		ps.println();
	}

	// === MOVE ===

	/**
	 * @apiNote déplacement vers le nord en nombre de pas
	 * @param nStep
	 */
	public void moveNorth(int nStep) {
		ISU.Vector v = isu.new Vector(0, -nStep * step.y());
		translate(v);
	}

	public void moveSouth(int nStep) {
		ISU.Vector v = isu.new Vector(0, nStep * step.y());
		translate(v);
	}

	public void moveEast(int nStep) {
		ISU.Vector v = isu.new Vector(nStep * step.x(), 0);
		translate(v);
	}

	public void moveWest(int nStep) {
		ISU.Vector v = isu.new Vector(-nStep * step.x(), 0);
		translate(v);
	}

	/**
	 * @apiNote déplacement vers l'est en cm
	 * @param length_cm
	 */
	public void moveNorth(double length_cm) {
		ISU.Vector v = isu.new Vector(0, -length_cm);
		translate(v);
	}

	public void moveSouth(double length_cm) {
		ISU.Vector v = isu.new Vector(0, length_cm);
		translate(v);
	}

	public void moveEast(double length_cm) {
		ISU.Vector v = isu.new Vector(length_cm, 0);
		translate(v);
	}

	public void moveWest(double length_cm) {
		ISU.Vector v = isu.new Vector(-length_cm, 0);
		translate(v);
	}

	// BOUNDING

	public void setBounding(Bounding bounding) {
		this.bounding = bounding;
		updateHitbox();
	}

	protected abstract void setBounding(Game game);

	// HITBOX

	/**
	 * Refresh the spatial representation (AABB + grid occupancy) from the bounding.
	 */
	private void updateHitbox() {
		if (bounding == null)
			return;
		getHitbox().rebuild(bounding.getShapes(), center);
	}

	// INTERSECTION

	public boolean intersects(Entity e) {
		if (bounding == null)
			throw new IllegalStateException("Bounding not set for entity: " + name);
		if (e.bounding == null)
			throw new IllegalStateException("Bounding not set for entity: " + e.name);
		// Broad phase: cell overlap test (delegated to the hitbox)
		if (!getHitbox().sharesCellsWith(e.getHitbox()))
			return false;
		// Narrow phase: exact shape intersection
		return bounding.intersects(e.bounding);
	}

	double distanceCenterToCenter(Entity e) {
		return center.distanceTo(e.center);
	}

	// DEPLOY in the Grid according to the BOUNDING

	public void deploy() {
		getHitbox().deploy();
	}

	public void retract() {
		getHitbox().retract();
	}

	// LIFECYCLE

	/**
	 * Detach this entity from the world so it can be garbage collected.
	 *
	 */
	public void destroy() {
		retract();
		if (bot != null) {
			bot.detach();
			bot = null;
		}
		stunt = null; // drop the actuator
		model = null; // no longer in any world
	}

	public Hitbox getHitbox() {
		return hitbox;
	}

	public void setHitbox(Hitbox hitbox) {
		this.hitbox = hitbox;
	}

}
