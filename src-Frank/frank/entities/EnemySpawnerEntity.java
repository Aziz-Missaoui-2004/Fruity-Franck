package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import frank.game.Categorized;
import frank.game.Category;


public class EnemySpawnerEntity extends Entity implements Categorized {

	public EnemySpawnerEntity(Game game, int x, int y) {
		super("EnemySpawner@" + x + "," + y, game);

		setSize(grid.new Dimension(1, 1).toISUDimension());
		setPosition(grid.new Position(x, y));
		setBounding(game);
	}

	@Override
	protected void setBounding(Game game) {
		Bounding b = new Bounding(center.mkCopy());

		// Petite hitbox invisible/logique.
		// On met un petit cercle pour que l'entité soit correctement enregistrée
		// dans les cellules du moteur sans gêner le gameplay.
		b.add(new Circle(center.mkCopy(), 0.10 * game.getCmPerCell()));

		setBounding(b);
	}

	@Override
	public Category category() {
		// WALL est pratique ici parce que FrankView ignore les entités WALL :
		// donc le spawner existe dans le modèle, mais il n'est pas dessiné.
		return Category.ENEMY_SPAWNER;
	}
}