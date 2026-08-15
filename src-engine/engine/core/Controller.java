package engine.core;

import engine.entities.Bot;
import oop.graphics.Canvas;

// == CONTROLLER ==

/**
 * Generic input listener, provided by the engine. It is the only piece that
 * touches the {@code oop} {@link Canvas}: it receives raw keyboard and mouse
 * events and forwards them to the player's {@link Bot}, which decides what each
 * input <i>means</i>.
 *
 * <p>
 * The game wires it by constructing it with the player bot and registering it
 * on the canvas ({@code canvas.set(controller)}). The bot behind it can be
 * swapped with {@link #setPlayerBot(Bot)} — e.g. when the player entity
 * respawns — so the controller stays registered for the canvas's lifetime while
 * the decision-maker changes underneath it.
 */
public class Controller implements Canvas.KeyListener, Canvas.MouseListener {

	/** The decision-maker that interprets the forwarded input; may be swapped. */
	private Bot playerBot;

	public Controller(Bot bot) {
		this.playerBot = bot;
	}

	/**
	 * Point the controller at a different bot (e.g. a freshly respawned player).
	 */
	public void setPlayerBot(Bot bot) {
		this.playerBot = bot;
	}

	// KEYBOARD: forward raw events to the bot, which decides their meaning.

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		if (playerBot != null)
			playerBot.pressed(keyCode, keyChar);
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		if (playerBot != null)
			playerBot.released(keyCode, keyChar);
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
	}

	// MOUSE: same generic forwarding for pointer-driven games.

	@Override
	public void moved(Canvas canvas, int x, int y) {
		if (playerBot != null)
			playerBot.mouseMoved(x, y);
	}

	@Override
	public void pressed(Canvas canvas, int bno, int x, int y) {
		if (playerBot != null)
			playerBot.mousePressed(bno, x, y);
	}

	@Override
	public void released(Canvas canvas, int bno, int x, int y) {
		if (playerBot != null)
			playerBot.mouseReleased(bno, x, y);
	}
}
