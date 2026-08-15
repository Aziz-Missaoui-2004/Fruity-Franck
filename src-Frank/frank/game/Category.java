package frank.game;

import engine.entities.Entity;

public enum Category {
    FRANK, ENEMY, ENEMY_SPAWNER, GRASS_HIGH, GRASS_LOW,
    APPLE, CHERRY, CHERRY_PROJECTILE, FRUIT, WALL;

    public static Category of(Entity e) {
        if (e instanceof Categorized c) return c.category();
        return null;
    }
}