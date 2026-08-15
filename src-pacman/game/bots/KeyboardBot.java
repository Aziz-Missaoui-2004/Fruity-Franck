package game.bots;

import engine.entities.Bot;
import engine.entities.Entity;
import engine.entities.Stunt;
import engine.geometry.ISU;
import oop.graphics.VirtualKeyCodes;

// == KEYBOARD BOT ==

/**
 * A keyboard-driven {@link Bot}: it receives key events forwarded by the engine
 * {@link engine.core.Controller} (via {@link Bot#pressed(int, char)}) and
 * decides what they mean — acting as the player's input FSM. Each arrow key
 * names a desired heading (constant velocity, cm per tick); {@code SPACE}
 * stops.
 *
 * <p>
 * It no longer touches the {@code oop} canvas itself; it only references the
 * key codes ({@link VirtualKeyCodes}) to declare the game's controls.
 */
public class KeyboardBot extends Bot {

	private final double speed; // cm per tick

	public KeyboardBot(Entity entity, double speed) {
		super(entity);
		this.speed = speed;
	}

	@Override
	public void pressed(int keyCode, char keyChar) {
		Stunt stunt = stunt();
		if (stunt == null)
			return;

		double dvx = 0;
		double dvy = 0;
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT -> dvx = speed;
		case VirtualKeyCodes.VK_LEFT -> dvx = -speed;
		case VirtualKeyCodes.VK_UP -> dvy = -speed;
		case VirtualKeyCodes.VK_DOWN -> dvy = speed;
		case VirtualKeyCodes.VK_SPACE -> {
			stunt.drive(0, 0); // stop now; also drops any buffered turn
			return;
		}
		default -> {
			return;
		}
		}

		ISU.Vector current = stunt.velocity();
		boolean moving = current != null && (current.x() != 0 || current.y() != 0);
		boolean perpendicular = moving && (current.x() != 0) != (dvx != 0);

		if (perpendicular)
			stunt.request(dvx, dvy); // can't turn mid-cell: remember it for the next crossing
		else
			stunt.drive(dvx, dvy); // start / reverse / same axis: take effect immediately
	}
}
