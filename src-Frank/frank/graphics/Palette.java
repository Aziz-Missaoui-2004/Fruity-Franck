package frank.graphics;

import java.util.EnumMap;

import frank.game.Category;

public final class Palette {

    private static final int[] DEFAULT = { 200, 200, 200 };
    private static final EnumMap<Category, int[]> RGB = new EnumMap<>(Category.class);

    static {
        RGB.put(Category.FRANK,      new int[]{ 255, 200,  50 });
        RGB.put(Category.ENEMY,      new int[]{ 220,  50,  50 });
        RGB.put(Category.GRASS_HIGH, new int[]{  34, 120,  24 });
        RGB.put(Category.GRASS_LOW,  new int[]{  18,  60,  12 });
        RGB.put(Category.APPLE,      new int[]{ 220,  30,  30 });
        RGB.put(Category.CHERRY,     new int[]{ 200,   0,   0 });
        RGB.put(Category.FRUIT,      new int[]{ 255, 130,   0 });
        RGB.put(Category.WALL,       new int[]{  30,  30,  30 });
    }

    private Palette() {}

    public static int[] rgb(Category category) {
        return RGB.getOrDefault(category, DEFAULT);
    }
}