package engine.graphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import engine.debug.Debug;
import engine.debug.DebugOverlay;
import engine.entities.Entity;
import oop.graphics.Graphics;

/**
 * Manages and paints all {@link Avatar}s in the correct draw order.
 *
 * Avatars are grouped by z-layer (see {@link Avatar#zOrder()}). Layers are
 * painted from the lowest z-value to the highest (back to front). Within each
 * layer, avatars are sorted by {@link Avatar#sOrder()} on every paint call,
 * which allows dynamic reordering (e.g. Y-based pseudo-depth) at minimal cost.
 *
 * The {@link TreeMap} provides natural ascending key order for z-layers
 */
public class View {

	/**
	 * Layers keyed by z-order. The TreeMap's natural ascending key order guarantees
	 * back-to-front painting across layers automatically.
	 */
	private final TreeMap<Integer, List<Avatar>> layers = new TreeMap<>();

	/**
	 * Optional camera mapping world coordinates to viewport pixels (world larger
	 * than the window). {@code null} means no camera — avatars render in absolute
	 * pixels
	 */
	private Camera camera;

	/** Optional debug config */
	private Debug debug;

	/**
	 * Attach a {@link Debug} so per-entity overlays (hitbox AABBs, automaton
	 * states) are drawn after the avatars when those flags are on. Pass
	 * {@code null} to detach.
	 */
	public void setDebug(Debug debug) {
		this.debug = debug;
	}

	/**
	 * @return the view's camera, or {@code null} if it renders in absolute pixels.
	 */
	public Camera camera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	// -------------------------------------------------------------------------
	// Avatar lifecycle
	// -------------------------------------------------------------------------

	/**
	 * Adds an avatar to the layer matching its {@link Avatar#zOrder()}.
	 */
	public void add(Avatar avatar) {
		layerFor(avatar.zOrder()).add(avatar);
	}

	/**
	 * Removes an avatar from the layer matching its current
	 * {@link Avatar#zOrder()}.
	 */
	public void remove(Avatar avatar) {
		List<Avatar> layer = layers.get(avatar.zOrder());
		if (layer != null)
			layer.remove(avatar);
	}

	/**
	 * Moves an avatar from one z-layer to another. Called internally by
	 * {@link Avatar#zOrder(int)}.
	 */
	void reorder(Avatar avatar, int oldZ, int newZ) {
		List<Avatar> oldLayer = layers.get(oldZ);
		if (oldLayer != null)
			oldLayer.remove(avatar);
		layerFor(newZ).add(avatar);
	}

	// -------------------------------------------------------------------------
	// Painting
	// -------------------------------------------------------------------------

	/**
	 * Paints all avatars in draw order: layers back-to-front (ascending z), and
	 * within each layer sorted by ascending {@link Avatar#sOrder()}.
	 */
	public void paint(Graphics g) {
		for (List<Avatar> layer : layers.values()) {
			layer.sort(Comparator.comparingDouble(Avatar::sOrder));
			for (Avatar avatar : layer)
				avatar.paint(g);
		}
		if (debug != null && debug.anyOverlay())
			paintDebugOverlays(g);
	}

	/**
	 * Draw the world-space debug overlays (hitbox AABBs and automaton states) on
	 * top of every avatar.
	 */
	private void paintDebugOverlays(Graphics g) {
		double ppc = debug.pixelPerCm();
		for (List<Avatar> layer : layers.values())
			for (Avatar avatar : layer) {
				Entity e = avatar.entity();
				if (e == null)
					continue;
				if (debug.showHitboxes())
					DebugOverlay.drawHitbox(g, e, camera, ppc);
				if (debug.showStates())
					DebugOverlay.drawState(g, e, camera, ppc);
				if (debug.showEntityNames())
					DebugOverlay.drawName(g, e, camera, ppc);
			}
	}

	// -------------------------------------------------------------------------

	private List<Avatar> layerFor(int z) {
		return layers.computeIfAbsent(z, k -> new ArrayList<>());
	}

}