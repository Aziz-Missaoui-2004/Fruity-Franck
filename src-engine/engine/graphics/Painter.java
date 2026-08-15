package engine.graphics;

import engine.debug.Debug;
import engine.debug.DebugOverlay;
import oop.graphics.Canvas;
import oop.graphics.Graphics;

/**
 * Entry point for the rendering pipeline.
 *
 * {@code Painter} is abstract because background rendering is game-specific —
 * subclasses must implement {@link #paintBackground(Canvas, Graphics)}.
 *
 */
public abstract class Painter implements Canvas.PaintListener {

	protected final View view;

	/** Optional debug config */
	private Debug debug;

	protected Painter(View view) {
		this.view = view;
	}

	/**
	 * Attach a {@link Debug} so each {@code paint} records its interval/FPS.Pass
	 * {@code null} to detach.
	 */
	public void setDebug(Debug debug) {
		this.debug = debug;
		view.setDebug(debug);
	}

	// -------------------------------------------------------------------------
	// Canvas.PaintListener
	// -------------------------------------------------------------------------

	@Override
	public void paint(Canvas canvas, Graphics g) {
		// Debug: record the interval since the previous paint, and derive
		// FPS
		if (debug != null && (debug.showPaintStats() || debug.showFps())) {
			double deltaMs = debug.paintStats().markAndSample();
			if (debug.showFps() && deltaMs > 0)
				debug.fpsStats().sample(1000.0 / deltaMs);
		}

		paintBackground(canvas, g);
		view.paint(g);
		paintForeground(canvas, g);

		// Debug: text HUD on top of everything (screen-space).
		if (debug != null && debug.anyHud())
			DebugOverlay.drawHud(g, debug);
	}

	/**
	 * Paints the background before any avatar is drawn. Implement this to fill,
	 * clear, or draw the game's background layer.
	 */
	protected abstract void paintBackground(Canvas canvas, Graphics g);

	/**
	 * Paints an overlay on top of all avatars (HUD, minimap, …). Default no-op.
	 */
	protected void paintForeground(Canvas canvas, Graphics g) {
	}

	/**
	 * Called when the canvas first becomes visible. Override to start a
	 * {@code RenderLoop} or initialize graphics resources.
	 */
	@Override
	public void visible(Canvas canvas) {
	}

	/**
	 * Called when the canvas is no longer visible. Override to pause a
	 * {@code RenderLoop} or release graphics resources.
	 */
	@Override
	public void revoked(Canvas canvas) {
	}
}