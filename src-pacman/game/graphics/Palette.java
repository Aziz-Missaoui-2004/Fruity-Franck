package game.graphics;

import java.util.EnumMap;
import game.Category;

public final class Palette {

    private static final int[] DEFAULT = { 200, 200, 200 };
    private static final EnumMap<Category, int[]> RGB = new EnumMap<>(Category.class);

    static {
        RGB.put(Category.PAC,   new int[]{ 255, 255,   0 });
        RGB.put(Category.GHOST, new int[]{ 255,   0,   0 });
        RGB.put(Category.GUM,   new int[]{ 255, 192, 203 });
        RGB.put(Category.BOSS,  new int[]{ 255,   0, 255 });
    }

    private Palette() {}

    public static int[] rgb(Category category) {   // ← game.Category, c'est tout
        return RGB.getOrDefault(category, DEFAULT);
    }
}