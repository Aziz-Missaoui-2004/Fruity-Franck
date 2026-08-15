package game;

import engine.core.Game;

public class PacMan extends Game {

	public PacMan(int w_ncell, int h_ncell, boolean torusOnX, boolean torusOnY, double cmPerCell, int pixelPerCm) {
		super(w_ncell, h_ncell, torusOnX, torusOnY, cmPerCell, pixelPerCm);
	}

	public PacMan(double w_cm, double h_cm, boolean torusOnX, boolean torusOnY, double cmPerCell, int pixelPerCm) {
		super(w_cm, h_cm, torusOnX, torusOnY, cmPerCell, pixelPerCm);
	}

}
