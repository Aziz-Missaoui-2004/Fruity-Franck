package frank.game;

import engine.core.Controller;
import engine.entities.Bot;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

public class FrankController extends Controller {

	private final FruityFrankModel model;
	private Runnable restartAction;

	public FrankController(Bot bot, FruityFrankModel model) {
		super(bot);
		this.model = model;
	}

	public void setRestartAction(Runnable restartAction) {
		this.restartAction = restartAction;
	}

	@Override
	public void pressed(Canvas canvas, int bno, int x, int y) {
		GameState st = model.state();

		if (st.isStartMenu()) {
			if (insideStartButton(canvas, x, y)) {
				st.startGame();
				return;
			}

			if (insideStartExitButton(canvas, x, y)) {
				GameApp.exitGame();
				return;
			}

			return;
		}

		if (st.isPaused()) {
			if (insidePauseResumeButton(canvas, x, y)) {
				st.setHoveredMenuOption(0);
				st.resumeGame();
				return;
			}

			if (insidePauseRestartButton(canvas, x, y)) {
				st.setHoveredMenuOption(0);
				if (restartAction != null)
					restartAction.run();
				return;
			}

			if (insidePauseExitButton(canvas, x, y)) {
				st.setHoveredMenuOption(0);
				GameApp.exitGame();
				return;
			}

			return;
		}

		if (st.isEnded() && st.canShowEndMenu()) {
			if (insideRestartButton(canvas, x, y)) {
				if (restartAction != null)
					restartAction.run();
				return;
			}

			if (insideExitButton(canvas, x, y)) {
				GameApp.exitGame();
				return;
			}

			return;
		}

		super.pressed(canvas, bno, x, y);
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		GameState st = model.state();

		// ESC : met le jeu en pause / le reprend (togglePause ignore menu et fin).
		if (keyCode == VirtualKeyCodes.VK_ESCAPE) {
			st.setHoveredMenuOption(0);
			st.togglePause();
			return;
		}

		// F3 : bascule les overlays de debug. Touche globale (rendu/dev),
		// pas une commande de Frank → gérée ici plutôt que dans FrankBot.
		if (keyCode == VirtualKeyCodes.VK_F3) {
			toggleDebugOverlay();
			return;
		}

		if (st.isPaused() || st.isEnded() || st.isStartMenu())
			return;

		super.pressed(canvas, keyCode, keyChar);
	}

	private void toggleDebugOverlay() {
		boolean show = !model.debugMode();
		GameApp.debug.setShowTickStats(show);
		GameApp.debug.setShowPaintStats(show);
		GameApp.debug.setShowFps(show);
		GameApp.debug.setShowHitboxes(show);
		GameApp.debug.setShowStates(show);
		GameApp.painter.setDebug(show ? GameApp.debug : null);
		model.toggleDebug();
	}

	private boolean inside(int mx, int my, int x, int y, int w, int h) {
		return mx >= x && mx <= x + w && my >= y && my <= y + h;
	}

	/**
	 * Teste si ({@code mx},{@code my}) tombe dans un bouton d'un menu. Le menu est
	 * une image {@code origW×origH} centrée et adaptée à la fenêtre ; le bouton est
	 * défini en fractions ({@code fx,fy,fw,fh}) de ce menu. Toutes les méthodes de
	 * hit-test délèguent ici (géométrie identique, seules les fractions changent).
	 */
	private boolean insideMenuButton(Canvas canvas, int mx, int my, int origW, int origH, double fx, double fy,
			double fw, double fh) {
		int cw = canvas.getWidth();
		int ch = canvas.getHeight();

		int menuW = Math.min(cw - 20, origW);
		int menuH = origH * menuW / origW;

		int menuX = (cw - menuW) / 2;
		int menuY = (ch - menuH) / 2;

		int x = menuX + (int) (fx * menuW);
		int y = menuY + (int) (fy * menuH);
		int w = (int) (fw * menuW);
		int h = (int) (fh * menuH);

		return inside(mx, my, x, y, w, h);
	}

	// End menu (image 713×276)
	private boolean insideRestartButton(Canvas canvas, int mx, int my) {
		return insideMenuButton(canvas, mx, my, 713, 276, 0.12, 0.45, 0.72, 0.22);
	}

	private boolean insideExitButton(Canvas canvas, int mx, int my) {
		return insideMenuButton(canvas, mx, my, 713, 276, 0.25, 0.68, 0.55, 0.22);
	}

	// Start & pause menus (image 819×330)
	private boolean insideStartButton(Canvas canvas, int mx, int my) {
		return insideMenuButton(canvas, mx, my, 819, 330, 0.18, 0.43, 0.65, 0.24);
	}

	private boolean insideStartExitButton(Canvas canvas, int mx, int my) {
		return insideMenuButton(canvas, mx, my, 819, 330, 0.27, 0.68, 0.55, 0.23);
	}

	private boolean insidePauseResumeButton(Canvas canvas, int mx, int my) {
		return insideMenuButton(canvas, mx, my, 819, 330, 0.30, 0.34, 0.42, 0.18);
	}

	private boolean insidePauseRestartButton(Canvas canvas, int mx, int my) {
		return insideMenuButton(canvas, mx, my, 819, 330, 0.17, 0.54, 0.67, 0.18);
	}

	private boolean insidePauseExitButton(Canvas canvas, int mx, int my) {
		return insideMenuButton(canvas, mx, my, 819, 330, 0.28, 0.74, 0.53, 0.17);
	}

	@Override
	public void moved(Canvas canvas, int x, int y) {
		GameState st = model.state();

		if (st.isStartMenu()) {
			if (insideStartButton(canvas, x, y))
				st.setHoveredMenuOption(1);
			else if (insideStartExitButton(canvas, x, y))
				st.setHoveredMenuOption(2);
			else
				st.setHoveredMenuOption(0);
			return;
		}

		if (st.isPaused()) {
			if (insidePauseResumeButton(canvas, x, y))
				st.setHoveredMenuOption(1);
			else if (insidePauseRestartButton(canvas, x, y))
				st.setHoveredMenuOption(2);
			else if (insidePauseExitButton(canvas, x, y))
				st.setHoveredMenuOption(3);
			else
				st.setHoveredMenuOption(0);
			return;
		}

		if (st.isEnded() && st.canShowEndMenu()) {
			if (insideRestartButton(canvas, x, y))
				st.setHoveredMenuOption(1);
			else if (insideExitButton(canvas, x, y))
				st.setHoveredMenuOption(2);
			else
				st.setHoveredMenuOption(0);
			return;
		}

		st.setHoveredMenuOption(0);
		super.moved(canvas, x, y);
	}
}