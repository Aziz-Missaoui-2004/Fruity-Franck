package frank.graphics;

import frank.entities.Enemy;
import frank.entities.Fruit;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public final class FrankSprites {

	private static final String PLAYER = "sprites/Fruity_Frank-player.png"; // 100x76
	private static final String ENEMY = "sprites/Fruity_Frank-enemies.png"; // 148x136
	private static final String ITEMS = "sprites/Fruity_Frank-Items.png"; // 124x64
	private static final String DIGITS = "sprites/sprites321-removebg-preview.png";
	private static final String GAME_OVER_MENU = "sprites/game-over_restart_exit.png";
	private static final String VICTORY_MENU = "sprites/victory_restart_exit.png";
	private static final String START_MENU = "sprites/fruity-frank_start.png";
	private static final String PAUSE_MENU = "sprites/echap_menu.png";
	private static final String MENU_CURSOR = "sprites/menu_cursor.png";
	
	// Géométrie EXACTE mesurée sur les images :
	// premier sprite à (4,5), taille 20x15, pas horizontal 24, pas vertical 20
	private static final int OX = 4, OY = 5; // origine
	private static final int SW = 20, SH = 15; // taille d'un sprite
	private static final int STEP_X = 24, STEP_Y = 20;

	private final BufferedImage[] frank = new BufferedImage[12]; // 4 col x 3 lignes
	private final BufferedImage[] enemy = new BufferedImage[24]; // 6 col x 4 lignes
	private final BufferedImage[] item = new BufferedImage[15]; // 5 col x 3 lignes
	private BufferedImage enemySpawner;
	private final BufferedImage[] digits = new BufferedImage[10];
	
	private BufferedImage gameOverMenu;
	private BufferedImage victoryMenu;
	private BufferedImage startMenu;
	private BufferedImage pauseMenu;
	private BufferedImage menuCursor;
	
	private boolean loaded = false;

	public void ensureLoaded(Graphics g) {
		if (loaded)
			return;

		BufferedImage ps = g.load(PLAYER);
		BufferedImage es = g.load(ENEMY);
		BufferedImage is = g.load(ITEMS);
		BufferedImage ds = g.load(DIGITS);
		gameOverMenu = g.load(GAME_OVER_MENU);
		victoryMenu = g.load(VICTORY_MENU);
		startMenu = g.load(START_MENU);
		pauseMenu = g.load(PAUSE_MENU);
		menuCursor = g.load(MENU_CURSOR);
		
		// FRANK : 4 colonnes × 3 lignes
		for (int r = 0; r < 3; r++)
			for (int c = 0; c < 4; c++)
				frank[r * 4 + c] = cell(ps, c, r);

		// ENNEMIS : 6 colonnes × 4 lignes
		for (int r = 0; r < 4; r++)
			for (int c = 0; c < 6; c++)
				enemy[r * 6 + c] = cell(es, c, r);

		// SPAWNER : ligne spéciale en bas de la spritesheet ennemis
		enemySpawner = cell(es, 0, 4);
		
		// ITEMS : 5 colonnes × 3 lignes
		for (int r = 0; r < 3; r++)
			for (int c = 0; c < 5; c++)
				item[r * 5 + c] = cell(is, c, r);

		// DIGITS : image avec 0 1 2 3 4 sur la première ligne,
		// puis 5 6 7 8 9 sur la deuxième ligne.
		if (ds != null) {
		    digits[0] = ds.getSubimage(5, 18, 75, 95);
		    digits[1] = ds.getSubimage(92, 18, 75, 95);
		    digits[2] = ds.getSubimage(182, 18, 75, 95);
		    digits[3] = ds.getSubimage(272, 18, 75, 95);
		    digits[4] = ds.getSubimage(360, 18, 65, 95);

		    digits[5] = ds.getSubimage(5, 145, 75, 95);
		    digits[6] = ds.getSubimage(92, 145, 75, 95);
		    digits[7] = ds.getSubimage(182, 145, 75, 95);
		    digits[8] = ds.getSubimage(272, 145, 75, 95);
		    digits[9] = ds.getSubimage(360, 145, 65, 95);
		}
		
		loaded = true;
	}
	
	public BufferedImage pauseMenu() {
	    return pauseMenu;
	}

	public BufferedImage startMenu() {
	    return startMenu;
	}
	
	public BufferedImage menuCursor() {
	    return menuCursor;
	}

	public BufferedImage victoryMenu() {
	    return victoryMenu;
	}
	
	public BufferedImage gameOverMenu() {
	    return gameOverMenu;
	}
	
	public BufferedImage digit(int n) {
	    if (n < 0 || n > 9)
	        return null;
	    return digits[n];
	}
	
	/** Découpe le sprite (colonne c, ligne r) avec la géométrie exacte mesurée. */
	private BufferedImage cell(BufferedImage sheet, int c, int r) {
		int x = OX + c * STEP_X;
		int y = OY + r * STEP_Y;
		if (sheet == null)
			return null;
		if (x + SW > sheet.getWidth() || y + SH > sheet.getHeight())
			return null;
		try {
			return sheet.getSubimage(x, y, SW, SH);
		} catch (Exception e) {
			return null;
		}
	}

	// FRANK : dir 0=droite 1=gauche 2=haut 3=bas
	public BufferedImage frank(int dir, int frame, boolean hasCherry) {
	    int row = switch (dir) {
	        case 0 -> 1; // droite
	        case 1 -> 0; // gauche
	        case 2 -> 2; // haut
	        case 3 -> 2; // bas
	        default -> 2;
	    };

	    /*
	     * Spritesheet Frank :
	     * colonnes 0-1 = avec projectile en main
	     * colonnes 2-3 = sans projectile
	     */
	    int baseCol = hasCherry ? 0 : 2;

	    int col = baseCol + (frame % 2);

	    return frank[row * 4 + col];
	}

	// ENEMY : ligne = type, colonnes par direction (0-1 droite, 2-3 gauche, 4-5 vertical)
	// dir : 0=droite 1=gauche 2=haut 3=bas
	public BufferedImage enemy(Enemy.EnemyType type, int dir, int frame) {
	    int row = switch (type) {
	        case ORANGE -> 0;
	        case PURPLE -> 1;
	        case GREEN  -> 2;
	        case RED    -> 3;
	    };
	    int baseCol = switch (dir) {
	        case 0 -> 2;  // droite → colonnes 0-1
	        case 1 -> 0;  // gauche → colonnes 2-3
	        case 2 -> 4;  // haut   → colonnes 4-5
	        case 3 -> 4;  // bas    → colonnes 4-5
	        default -> 0;
	    };
	    int col = baseCol + (frame % 2); // anime entre les 2 frames de la direction
	    return enemy[row * 6 + col];
	}
	
	// FRUITS — index identifiés par analyse des couleurs
	public BufferedImage fruit(Fruit.FruitType type) {
		return switch (type) {
		case BANANA -> item[5]; // banane jaune
		case ORANGE -> item[1]; // fruit rouge/orange
		case STRAWBERRY -> item[8]; // fraise rouge
		case PLUM -> item[6]; // prune violette
		case GRAPES -> item[10]; // raisin
		};
	}

	public BufferedImage apple() {
		return item[12];
	} // grosse pomme orange/rouge

	public BufferedImage cherry() {
		return item[4];
	} // cerises rouge foncé
	
	public BufferedImage enemySpawner() {
	    return enemySpawner;
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	public BufferedImage countdownDigit(int n) {
	    if (n < 1 || n > 3)
	        return null;
	    return digits[n];
	}
}