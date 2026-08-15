package engine.debug;

import java.util.ArrayList;
import java.util.List;

import engine.entities.Bot;
import engine.entities.Entity;
import engine.entities.Hitbox;
import engine.gal.aut.Category;
import engine.gal.aut.State;
import engine.graphics.Camera;
import oop.graphics.Font;
import oop.graphics.Graphics;

// == DEBUG OVERLAY ==

/**
 * Drawing helpers for the {@link Debug} features.
 */
public final class DebugOverlay {

	private DebugOverlay() {
	}

	// SCREEN-SPACE HUD

	/**
	 * Draw the enabled metric lines (tick / paint / fps) as a small text block in
	 * the top-left corner. Values are millisecond intervals; fps is frames/second.
	 */
	public static void drawHud(Graphics g, Debug d) {
	    int x = 8;
	    int y = 16;
	    int dy = 14;

	    
	    List<String> lines = new ArrayList<>();
	    if (d.showTickStats())  lines.add(line("tick ms",  d.tickStats()));
	    if (d.showPaintStats()) lines.add(line("paint ms", d.paintStats()));
	    if (d.showFps())        lines.add(line("fps",      d.fpsStats()));
	    if (lines.isEmpty())
	        return;

	    Font f  = g.getFont();
	    int asc = f.getAscent();
	    int dsc = f.getDescent();
	    int pad = 3;

	    // largeur de la plus longue ligne 
	    int w = 0;
	    for (String s : lines)
	        w = Math.max(w, f.getWidth(s));
	    int h = (lines.size() - 1) * dy + asc + dsc;

	    // fond noir derrière tout le bloc
	    g.setColor(g.getColor(255, 0, 0, 0));
	    g.fillRect(x - pad, y - asc - pad, w + 2 * pad, h + 2 * pad);

	    //le text de debug
	    g.setColor(g.getColor(255, 255, 255, 0)); // opaque yellow
	    for (String s : lines) {
	        g.drawString(s, x, y);
	        y += dy;
	    }
	}

	private static String line(String label, Stats s) {
		return String.format("%-9s min %6.2f  max %6.2f  avg %6.2f", label, s.min(), s.max(), s.avg());
	}

	// WORLD-SPACE PER-ENTITY OVERLAYS

	/**
	 * Outline the entity's axis-aligned bounding box (AABB), in cyan.
	 */
	public static void drawHitbox(Graphics g, Entity e, Camera cam, double ppc) {
		Hitbox h = e.getHitbox();
		if (h == null)
			return;
		double scale = (cam != null) ? cam.pixelPerCm() : ppc;
		if (scale <= 0)
			return; // no way to size the box (no camera and no ppc fallback)
		double cxCm = (h.getMinX() + h.getMaxX()) / 2;
		double cyCm = (h.getMinY() + h.getMaxY()) / 2;
		int[] c = toScreen(cxCm, cyCm, cam, ppc);
		if (c == null)
			return;
		int rw = (int) Math.round((h.getMaxX() - h.getMinX()) * scale);
		int rh = (int) Math.round((h.getMaxY() - h.getMinY()) * scale);
		g.setColor(g.getColor(255, 0, 255, 255)); // cyan
		g.drawRect(c[0] - rw / 2, c[1] - rh / 2, rw, rh);
	}

	/**
	 * Draw the entity's current automaton {@link State} (its {@code mode_id}) just
	 * above its centre, in white.
	 */
	public static void drawState(Graphics g, Entity e, Camera cam, double ppc) {
		Bot bot = e.bot();
		if (bot == null)
			return;
		State s = bot.state();
		if (s == null)
			return;
		int[] c = toScreen(e.center().x(), e.center().y(), cam, ppc);
		if (c == null)
			return;
		g.setColor(g.getColor(255, 255, 255, 255)); // white
		g.drawString(s.toString(), c[0] + 6, c[1] - 6);
	}

	/**
	 * Draw the entity's name
	 */
	public static void drawName(Graphics g, Entity e, Camera cam, double ppc) {
		String name = e.name();

		if (name == null)
			return;

		int[] c = toScreen(e.center().x(), e.center().y(), cam, ppc);
		if (c == null)
			return;

		g.setColor(g.getColor(220, 255, 255, 0));
		g.drawString(name, c[0] + 6, c[1] - 16);
	}

	// HELPERS

	/**
	 * Map a world point (cm) to screen pixels, preferring the camera and falling
	 * back to a plain ppc scaling. Returns {@code null} if neither is available.
	 */
	private static int[] toScreen(double cmX, double cmY, Camera cam, double ppc) {
		if (cam != null)
			return cam.worldToScreen(cmX, cmY);
		if (ppc > 0)
			return new int[] { (int) Math.round(cmX * ppc), (int) Math.round(cmY * ppc) };
		return null;
	}
}
