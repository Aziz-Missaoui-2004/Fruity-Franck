package frank.graphics;

import engine.entities.Entity;
import engine.graphics.Camera;
import engine.graphics.Painter;
import frank.entities.Frank;
import frank.game.Category;
import frank.game.FruityFrankMap;
import frank.game.FruityFrankModel;
import frank.game.GameState;
import oop.graphics.Canvas;
import oop.graphics.Color;
import oop.graphics.Font;
import oop.graphics.Graphics;
import oop.graphics.BufferedImage;

public class FrankPainter extends Painter {

	private final FrankView view;
	private final FruityFrankMap map;
	private final FruityFrankModel model;
	private final int cellPx;
	private final double cmPerCell;

	public FrankPainter(FrankView view, FruityFrankMap map, FruityFrankModel model, int cellPx, double cmPerCell) {
		super(view);
		this.view = view;
		this.map = map;
		this.model = model;
		this.cellPx = cellPx;
		this.cmPerCell = cmPerCell;
	}

	@Override
	protected void paintBackground(Canvas canvas, Graphics g) {
		g.setColor(g.getColor(255, 0, 0, 0));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		// murs
		Camera cam = view.camera();
		g.setColor(g.getColor(255, 40, 40, 40));
		for (int c = 0; c < map.cols(); c++)
			for (int r = 0; r < map.rows(); r++) {
				if (!map.isWall(c, r))
					continue;
				double wx = (c + 0.5) * cmPerCell, wy = (r + 0.5) * cmPerCell;
				if (cam != null) {
					if (!cam.isVisible(wx, wy, cmPerCell))
						continue;
					int[] s = cam.worldToScreen(wx, wy);
					g.fillRect(s[0] - cellPx / 2, s[1] - cellPx / 2, cellPx, cellPx);
				} else
					g.fillRect(c * cellPx, r * cellPx, cellPx, cellPx);
			}
	}

	@Override
	protected void paintForeground(Canvas canvas, Graphics g) {
		GameState st = model.state();
		int cw = canvas.getWidth();

		// HUD
		g.setColor(g.getColor(255, 0, 0, 0));
		g.fillRect(0, 0, cw, 24);
		g.setColor(g.getColor(255, 255, 220, 0));
		g.drawString("SCORE " + String.format("%05d", st.score()), 8, 16);
		g.drawString("HI " + String.format("%05d", st.highScore()), cw / 2 - 30, 16);
		g.drawString("FRUITS " + st.fruitsRemaining(), 90, 16);

		// compteur d'ennemis : entre le HI SCORE et les vies, calé à gauche des vies
		String enemies = "ENEMIES " + model.enemyCount();
		g.drawString(enemies, cw - 64 - g.getFont().getWidth(enemies), 16);

		Frank frank = model.player();
		if (frank != null) {
			g.setColor(g.getColor(255, 255, 120, 0));
			int lives = st.lives();
			for (int i = 0; i < lives; i++)
			    g.fillOval(cw - 24 - i * 16, 6, 12, 12);
		}


		paintStartMenu(canvas, g);

		paintPauseMenu(canvas, g);

		if (!st.isStartMenu()) {
		    paintEndMenu(canvas, g);
		    paintCountdown(canvas, g);
		}

		if (model.debugMode())
			debugOverlay(g);
	}
	private void drawLabel(Graphics g, String text, int x, int y, Color textColor) {
	    Font f = g.getFont();
	    int w   = f.getWidth(text);   // largeur réelle du texte
	    int asc = f.getAscent();      // au-dessus de la ligne de base
	    int dsc = f.getDescent();     // en dessous
	    int pad = 2;

	    // fond noir opaque 
	    g.setColor(g.getColor(255, 0, 0, 0));
	    g.fillRect(x - pad, y - asc - pad, w + 2 * pad, asc + dsc + 2 * pad);

	    // texte
	    g.setColor(textColor);
	    g.drawString(text, x, y);
	}
	private void debugOverlay(Graphics g) {
	    Camera cam = view.camera();
	    Color frankColor = g.getColor(255, 255, 80, 80);
	    Color otherColor = g.getColor(220, 255, 255, 0);
	    int lives = model.state().lives();

	    for (Entity e : model.entities()) {
	        Category cat = Category.of(e);
	        int[] s = (cam != null) ? cam.worldToScreen(e.center().x(), e.center().y())
	                : new int[] { (int) e.center().x(), (int) e.center().y() };
	        int lx = s[0] - 20;
	        int ly = s[1] - cellPx / 2 - 4;

	        if (cat == Category.FRANK && e instanceof Frank f) {
	            drawLabel(g, f.name() + " vies:" + lives, lx, ly, frankColor);
	        } else if (cat == Category.ENEMY || cat == Category.APPLE) {
	            drawLabel(g, e.name(), lx, ly, otherColor);
	        }
	    }
	}
	
	
	/** Dessine {@code img} centrée et adaptée à la fenêtre ; renvoie {@code {x, y, w, h}} affichés. */
	private int[] drawCenteredImage(Canvas canvas, Graphics g, BufferedImage img) {
	    int cw = canvas.getWidth();
	    int iw = img.getWidth();

	    int w = Math.min(cw - 20, iw);
	    int h = img.getHeight() * w / iw;
	    int x = (cw - w) / 2;
	    int y = (canvas.getHeight() - h) / 2;

	    g.drawImage(img, x, y, w, h);
	    return new int[] { x, y, w, h };
	}

	private void paintPauseMenu(Canvas canvas, Graphics g) {
	    if (!model.state().isPaused())
	        return;

	    BufferedImage img = view.sprites().pauseMenu();
	    if (img == null)
	        return;

	    int[] r = drawCenteredImage(canvas, g, img);
	    paintPauseMenuCursor(g, r[0], r[1], r[2], r[3]);
	}

	private void paintStartMenu(Canvas canvas, Graphics g) {
	    if (!model.state().isStartMenu())
	        return;

	    BufferedImage img = view.sprites().startMenu();
	    if (img == null)
	        return;

	    int[] r = drawCenteredImage(canvas, g, img);
	    paintMenuCursor(canvas, g, r[0], r[1], r[2], r[3], true);
	}
	
	private void paintCountdown(Canvas canvas, Graphics g) {
	    GameState st = model.state();
	    if (!st.isCountingDown())
	        return;

	    BufferedImage img = view.sprites().countdownDigit(st.countdownNumber());

	    if (img == null)
	        return;

	    int w = 90;
	    int h = 110;

	    int x = (canvas.getWidth() - w) / 2;
	    int y = (canvas.getHeight() - h) / 2;

	    g.drawImage(img, x, y, w, h);
	}
	
	private void paintEndMenu(Canvas canvas, Graphics g) {
	    GameState st = model.state();
	    if (!st.isEnded() || !st.canShowEndMenu())
	        return;

	    BufferedImage img = st.isVictory()
	            ? view.sprites().victoryMenu()
	            : view.sprites().gameOverMenu();
	    if (img == null)
	        return;

	    int[] r = drawCenteredImage(canvas, g, img);
	    paintMenuCursor(canvas, g, r[0], r[1], r[2], r[3], true);
	}
	private void paintMenuCursor(Canvas canvas, Graphics g, int menuX, int menuY, int menuW, int menuH, boolean startMenu) {
	    int option = model.state().hoveredMenuOption();

	    if (option == 0)
	        return;

	    BufferedImage cursor = view.sprites().menuCursor();

	    if (cursor == null)
	        return;

	    int cursorW = Math.max(12, (int) (0.045 * menuW));
	    int cursorH = cursor.getHeight() * cursorW / cursor.getWidth();

	    double cursorXRatio;
	    double cursorYRatio;

	    if (startMenu) {
	        cursorXRatio = 0.12;
	        cursorYRatio = option == 1 ? 0.64 : 0.86;
	    } else {
	        cursorXRatio = 0.09;
	        cursorYRatio = option == 1 ? 0.68 : 0.90;
	    }

	    int x = menuX + (int) (cursorXRatio * menuW) - cursorW;
	    int y = menuY + (int) (cursorYRatio * menuH) - cursorH / 2;

	    g.drawImage(cursor, x, y, cursorW, cursorH);
	}
	
	private void paintPauseMenuCursor(Graphics g, int menuX, int menuY, int menuW, int menuH) {
	    int option = model.state().hoveredMenuOption();

	    if (option == 0)
	        return;

	    BufferedImage cursor = view.sprites().menuCursor();

	    if (cursor == null)
	        return;

	    int cursorW = Math.max(12, (int) (0.045 * menuW));
	    int cursorH = cursor.getHeight() * cursorW / cursor.getWidth();

	    double cursorXRatio = 0.10;
	    double cursorYRatio;

	    if (option == 1) {
	        cursorYRatio = 0.51; // RESUME
	    } else if (option == 2) {
	        cursorYRatio = 0.68; // RESTART GAME
	    } else {
	        cursorYRatio = 0.89; // EXIT GAME
	    }

	    int x = menuX + (int) (cursorXRatio * menuW) - cursorW;
	    int y = menuY + (int) (cursorYRatio * menuH) - cursorH / 2;

	    g.drawImage(cursor, x, y, cursorW, cursorH);
	}
}
