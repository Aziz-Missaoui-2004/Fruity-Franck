package engine.graphics;

import engine.entities.Entity;
import oop.graphics.Graphics;

/**
 * Visual counterpart of a game {@link Entity}.
 *
 * Each Avatar belongs to a z-layer ({@link #zOrder}) that determines which
 * group it is drawn in relative to other layers (lower = drawn first). Within a
 * layer, avatars are sorted by {@link #sOrder()}, which defaults to 0.0 but can
 * be overridden
 *
 */
public abstract class Avatar {

	/** The mode an avatar starts in until the behaviour layer sets another. */
	public static final String DEFAULT = "default";

	protected final Entity entity;
	protected final View view;

	private int zOrder;
	private String mode = DEFAULT;

	/**
	 * @param entity the game entity this avatar represents
	 * @param view   the view this avatar belongs to
	 * @param zOrder the layer this avatar is drawn in (lower = further back)
	 */
	protected Avatar(Entity entity, View view, int zOrder) {
		this.entity = entity;
		this.view = view;
		this.zOrder = zOrder;
	}

	// -------------------------------------------------------------------------
	// Painting
	// -------------------------------------------------------------------------

	/**
	 * Paints this avatar using the given graphics context. Called by
	 * {@link View#paint(Graphics)} in the correct draw order.
	 */
	public abstract void paint(Graphics g);

	// -------------------------------------------------------------------------
	// Ordering
	// -------------------------------------------------------------------------

	/**
	 * Returns the layer this avatar belongs to. Lower values are drawn first
	 * (further back).
	 */
	public int zOrder() {
		return zOrder;
	}

	/**
	 * Moves this avatar to a different z-layer. Notifies the view so it can update
	 * its internal layer structure.
	 *
	 * @param newZ the new layer index
	 */
	public void zOrder(int newZ) {
		if (newZ == this.zOrder)
			return;
		view.reorder(this, this.zOrder, newZ);
		this.zOrder = newZ;
	}

	/**
	 * Sort key within a z-layer. Avatars with a lower value are drawn first.
	 *
	 * Defaults to 0.0. Override to enable fine-grained ordering — for example,
	 * returning the entity's Y position produces correct pseudo-depth ordering in
	 * top-down games (entities lower on screen appear in front).
	 */
	public double sOrder() {
		return 0.0;
	}

	// -------------------------------------------------------------------------
	// Mode (behaviour-driven appearance)
	// -------------------------------------------------------------------------

	/** @return the game entity this avatar represents. */
	public Entity entity() {
		return entity;
	}

	/** @return the current appearance mode (see the class javadoc). */
	public String mode() {
		return mode;
	}

	/**
	 * Set the appearance mode. Called by the behaviour layer (the entity's bot, via
	 * {@code entity.getAvatar()}) when its FSM mode changes; the next
	 * {@link #paint(Graphics)} reflects it.
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}
}