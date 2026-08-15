package frank.game;

public class GameState {
	private int score;
	private int highScore;
	int lives = 3;

	private boolean startMenu = true;
	private boolean paused = false;
	private boolean gameOver;
	private boolean frankDied;

	private int fruitsRemaining;
	private int totalFruits;
	private boolean victory;

	private int countdownTicks = 90;

	private static final int END_SCREEN_DELAY = 30; // 1 seconde à 30 TPS
	private int endScreenTicks = -1;

	// Etat survol
	private int hoveredMenuOption; // 0 = rien, 1 = bouton du haut, 2 = bouton du bas , 3 = bouton du bas_bas

	public int lives() {
		return this.lives;
	}

	public int score() {
		return score;
	}

	public int highScore() {
		return highScore;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public boolean frankDied() {
		return frankDied;
	}

	public boolean isVictory() {
		return victory;
	}

	public boolean isStartMenu() {
		return startMenu;
	}

	public void startGame() {
		startMenu = false;
		hoveredMenuOption = 0;
	}

	public int fruitsRemaining() {
		return fruitsRemaining;
	}

	public void clearDeath() {
		frankDied = false;
	}

	public void addScore(int p) {
		score += p;
		if (score > highScore)
			highScore = score;
	}

	public void setGameOver() {
		gameOver = true;
		startEndScreenDelay();
	}

	public void killedByEnemy() {
		if (gameOver || victory)
			return;

		loseLife();

		if (lives <= 0) {
			setGameOver();
		} else {
			frankDied = true;
		}
	}

	public void setTotalFruits(int n) {
		totalFruits = n;
		setFruitsRemaining(n);
	}

	public void setFruitsRemaining(int n) {
		fruitsRemaining = Math.max(0, n);
		victory = fruitsRemaining == 0;
		if (victory)
			startEndScreenDelay();
	}

	public void collectFruit() {
		if (gameOver || victory)
			return;

		fruitsRemaining--;

		if (fruitsRemaining <= 0) {
			fruitsRemaining = 0;
			victory = true;
			startEndScreenDelay();
		}
	}

	public void loseLife() {
		if (lives > 0)
			lives--;
	}

	public boolean isCountingDown() {
		return countdownTicks > 0;
	}

	public void tickCountdown() {
		if (countdownTicks > 0)
			countdownTicks--;
	}

	public int countdownNumber() {
		if (countdownTicks > 60)
			return 3;
		if (countdownTicks > 30)
			return 2;
		if (countdownTicks > 0)
			return 1;
		return 0;
	}

	private void startEndScreenDelay() {
		if (endScreenTicks < 0)
			endScreenTicks = END_SCREEN_DELAY;
	}

	public void tickEndScreenDelay() {
		if (endScreenTicks > 0)
			endScreenTicks--;
	}

	public boolean canShowEndMenu() {
		return endScreenTicks == 0;
	}

	public boolean isEnded() {
		return gameOver || victory;
	}

	public void reset() {
		score = 0;
		lives = 3;

		paused = false;
		startMenu = false;
		hoveredMenuOption = 0;
		gameOver = false;
		frankDied = false;

		fruitsRemaining = 0;
		victory = false;

		countdownTicks = 90;

		endScreenTicks = -1;
	}

	public boolean isPaused() {
		return paused;
	}

	public void resumeGame() {
		paused = false;
	}

	public void togglePause() {
		if (startMenu || isEnded())
			return;
		paused = !paused;
	}

	public int hoveredMenuOption() {
		return hoveredMenuOption;
	}

	public void setHoveredMenuOption(int option) {
		hoveredMenuOption = option;
	}

	public int totalFruit() {
		return totalFruits;
	}
}