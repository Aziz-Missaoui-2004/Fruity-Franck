package game.graphics;

import engine.core.Model;
import engine.entities.Entity;
import engine.graphics.Camera;
import engine.graphics.Minimap;
import engine.graphics.Painter;
import game.Maze;
import game.Category;
import oop.graphics.Canvas;
import oop.graphics.Graphics;

// == PACMAN PAINTER ==

/**
 * The Pac-Man rendering entry point. Background: black, then the maze walls as
 * blue blocks, drawn through the {@link Camera} so they scroll with the world.
 * Foreground: a corner {@link Minimap} of the whole world — walls, entity dots,
 * and the current viewport rectangle — drawn on top of the avatars.
 */
public class PacManPainter extends Painter {

	private final PacManView view;
	private final Maze maze;
	private final int cellPx;
	private final double cmPerCell;
	private final Minimap minimap;

	public PacManPainter(PacManView view, Maze maze, int cellPx, double cmPerCell, Minimap minimap) {
		super(view);
		this.view = view;
		this.maze = maze;
		this.cellPx = cellPx;
		this.cmPerCell = cmPerCell;
		this.minimap = minimap;
	}

	@Override
	protected void paintBackground(Canvas canvas, Graphics g) {
		g.setColor(g.getColor(255, 0, 0, 0));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		if (maze == null)
			return;
		g.setColor(g.getColor(255, 33, 33, 222)); // classic maze blue
		Camera cam = view.camera();
		for (int c = 0; c < maze.cols(); c++)
			for (int r = 0; r < maze.rows(); r++) {
				if (!maze.isWall(c, r))
					continue;
				double ccx = (c + 0.5) * cmPerCell;
				double ccy = (r + 0.5) * cmPerCell;
				if (cam != null) {
					if (!cam.isVisible(ccx, ccy, cmPerCell))
						continue;
					int[] s = cam.worldToScreen(ccx, ccy);
					g.fillRect(s[0] - cellPx / 2, s[1] - cellPx / 2, cellPx, cellPx);
				} else {
					g.fillRect(c * cellPx, r * cellPx, cellPx, cellPx);
				}
			}
	}

	@Override
	protected void paintForeground(Canvas canvas, Graphics g) {
		if (minimap == null)
			return;
		int dot = Math.max(2, (int) Math.round(cmPerCell * minimap.scaleX()));

		// Panel background.
		g.setColor(g.getColor(200, 0, 0, 0));
		g.fillRect(minimap.panelX(), minimap.panelY(), minimap.panelW(), minimap.panelH());

		// Walls.
		g.setColor(g.getColor(255, 33, 33, 222));
		for (int c = 0; c < maze.cols(); c++)
			for (int r = 0; r < maze.rows(); r++)
				if (maze.isWall(c, r)) {
					int[] p = minimap.worldToPanel((c + 0.5) * cmPerCell, (r + 0.5) * cmPerCell);
					g.fillRect(p[0], p[1], dot, dot);
				}

		// Entity dots, coloured by category.
		for (Entity e : view.model().entities()) {
			int[] rgb = Palette.rgb(Category.of(e));
			g.setColor(g.getColor(255, rgb[0], rgb[1], rgb[2]));
			int[] p = minimap.worldToPanel(e.center().x(), e.center().y());
			g.fillOval(p[0] - dot, p[1] - dot, 2 * dot, 2 * dot);
		}

		// Viewport rectangle (where the camera is looking).
		Camera cam = view.camera();
		if (cam != null)
			drawViewportRect(g, cam);
	}

	/** Outline the camera's viewport on the minimap with four thin bars. */
	private void drawViewportRect(Graphics g, Camera cam) {
		double left = cam.centreX() - cam.viewWidthCm() / 2;
		double top = cam.centreY() - cam.viewHeightCm() / 2;
		int[] tl = minimap.worldToPanel(left, top);
		int w = (int) Math.round(cam.viewWidthCm() * minimap.scaleX());
		int h = (int) Math.round(cam.viewHeightCm() * minimap.scaleY());
		g.setColor(g.getColor(255, 255, 255, 255));
		g.fillRect(tl[0], tl[1], w, 1);
		g.fillRect(tl[0], tl[1] + h, w, 1);
		g.fillRect(tl[0], tl[1], 1, h);
		g.fillRect(tl[0] + w, tl[1], 1, h);
	}
}
