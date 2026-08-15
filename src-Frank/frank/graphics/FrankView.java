package frank.graphics;

import java.util.HashSet;
import java.util.Set;

import engine.core.Game;
import engine.core.Model;
import engine.entities.Entity;
import engine.graphics.Avatar;
import engine.graphics.Camera;
import engine.graphics.View;
import frank.game.Category;
import oop.graphics.Graphics;

public class FrankView extends View {

    private final Game game;
    private final Model model;
    private final FrankSprites sprites;
    private final int cellPx;
    private final Set<Entity> tracked = new HashSet<>();

    public FrankView(Game game, Model model, FrankSprites sprites) {
        this.game = game; this.model = model; this.sprites = sprites;
        this.cellPx = (int) Math.round(game.getCmPerCell() * game.getPixelPerCm());
        sync();
    }

    public void sync() {
        for (Entity e : model.entities()) {
            if (e.getAvatar() != null) continue;
            if (Category.of(e) == Category.WALL) continue;
            Category cat = Category.of(e);
            FrankAvatar a = new FrankAvatar(e, this, zOrderFor(cat),
                    game.getPixelPerCm(), cellPx, cat, sprites);
            e.setAvatar(a); add(a); tracked.add(e);
        }
        tracked.removeIf(e -> {
            if (model.entities().contains(e)) return false;
            Avatar a = e.getAvatar();
            if (a != null) { remove(a); e.setAvatar(null); }
            return true;
        });
    }

    @Override
    public void paint(Graphics g) { sync(); updateCamera(); super.paint(g); }

    private void updateCamera() {
        Camera cam = camera();
        if (cam == null) return;
        for (Entity e : model.entities())
            if (Category.of(e) == Category.FRANK) {
                cam.setTarget(e.center().x(), e.center().y()); break;
            }
        cam.update();
    }

    public Model model() { return model; }
    public FrankSprites sprites() {
    	return this.sprites;
    }

    private int zOrderFor(Category cat) {
        return switch (cat) {
            case GRASS_LOW  -> 0;
            case GRASS_HIGH -> 1;
            case FRUIT, CHERRY, CHERRY_PROJECTILE -> 2;
            case ENEMY, ENEMY_SPAWNER  -> 3;
            case FRANK      -> 4;
            case APPLE      -> 5;
            default         -> 2;
        };
    }
}
