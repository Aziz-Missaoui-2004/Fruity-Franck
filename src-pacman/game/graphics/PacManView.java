package game.graphics;

import java.util.HashSet;
import java.util.Set;

import engine.core.Game;
import engine.core.Model;
import engine.entities.Entity;
import engine.graphics.Avatar;
import engine.graphics.Camera;
import engine.graphics.View;
import game.Category;
import oop.graphics.Graphics;

// == PACMAN VIEW ==

/**
 * Pac-Man's look: gives every live {@link Entity} a {@link PacManAvatar} and
 * paints them through the engine's z-order {@link View}. The avatar is stored
 * on the entity itself ({@link Entity#setAvatar}), so the link is two-way — the
 * view reads the entity to draw it, and the behaviour layer reaches the avatar
 * through the entity to drive its appearance (see {@link Avatar#mode()}).
 *
 * <p>
 * The set is reconciled with the model every frame ({@link #sync()}): new
 * entities get an avatar, and an entity that has left the world has its avatar
 * dropped from the view and cleared from the entity. The view tracks only
 * membership ({@link #tracked}); the avatar handle lives on the entity.
 */
public class PacManView extends View {

	private final Game game;
	private final Model model;
	private final Sprites sprites;
	private final int cellPx;
	/** Entities this view has created an avatar for (membership only). */
	private final Set<Entity> tracked = new HashSet<>();

	public PacManView(Game game, Model model, Sprites sprites) {
		this.game = game;
		this.model = model;
		this.sprites = sprites;
		this.cellPx = (int) Math.round(game.getCmPerCell() * game.getPixelPerCm());
		sync();
	}

	/**
	 * Reconcile with the live entity set: create + attach an avatar for every
	 * entity that lacks one, and drop the avatar of every entity that has left the
	 * world.
	 */
	public void sync() {
		for (Entity e : model.entities()) {
			if (e.getAvatar() == null) {
				Category cat = Category.of(e);
				Avatar a = new PacManAvatar(e, this, zOrderFor(cat), game.getPixelPerCm(), cellPx, cat, sprites);
				e.setAvatar(a); // two-way link
				add(a);
				tracked.add(e);
			}
		}
		tracked.removeIf(e -> {
			if (!model.entities().contains(e)) {
				Avatar a = e.getAvatar();
				if (a != null) {
					remove(a);
					e.setAvatar(null);
				}
				return true;
			}
			return false;
		});
	}

	/** Sync, advance the camera to follow the player, then draw in z-order. */
	@Override
	public void paint(Graphics g) {
		sync();
		updateCamera();
		super.paint(g);
	}

	/** Point the camera at the current player Pac and ease it forward. */
	private void updateCamera() {
		Camera cam = camera();
		if (cam == null)
			return;
		for (Entity e : model.entities())
			if (Category.of(e) == Category.PAC) {
				cam.setTarget(e.center().x(), e.center().y());
				break;
			}
		cam.update();
	}

	/** @return the model this view renders (for the painter's minimap). */
	public Model model() {
		return model;
	}

	/** Gums sit behind the actors. */
	private int zOrderFor(Category cat) {
		return cat == Category.GUM ? 0 : 1;
	}
}
