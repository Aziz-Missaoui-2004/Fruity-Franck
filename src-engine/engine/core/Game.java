package engine.core;
// == GAME ==

import java.io.PrintStream;

import engine.geometry.Grid;
import engine.geometry.ISU;

public abstract class Game {

	private final boolean torusOnXaxis; // vrai si l'axe X est une boucle fermée
	private final boolean torusOnYaxis; // vrai si l'axe Y est une boucle fermée

	private final double cmPerCell; // échelle qui relie l'unité ncell à cm
	private final int pixelPerCm; // échelle qui relie l'unité pixel à cm

	/**
	 * World gravity (cm/tick² conceptually; games that model "no acceleration"
	 * Boulder-Dash-style falling simply read it as an on/off + speed cue). 0 = no
	 * gravity by default. Set via
	 * {@link #setGravity(double)} so existing constructors stay unchanged.
	 */
	private double gravity = 0.0;

	// FIELDS

	private final int width_ncell; // largeur du monde en nombre de cellules
	private final int height_ncell; // hauteur du monde en nombre de celluls

	private final double width_cm; // largeur du monde en cm
	private final double height_cm; // hauteur du monde en cm

	private final Grid grid; // permet la création de coordonnées en unités ncell
	private final ISU isu; // permet la création de coordonnées en unités cm
	// Picture pict; // permet la création de coordonnées en unités pixel, ne sera
	// utilisé qu'à partir de Task2

	// CONSTRUCTORS

	protected Game(int w_ncell, int h_ncell, boolean torusOnX, boolean torusOnY, double cmPerCell, int pixelPerCm) {
		assert w_ncell > 0 : "Width must be positive";
		assert h_ncell > 0 : "Height must be positive";
		assert cmPerCell > 0 : "cmPerCell must be positive";
		assert pixelPerCm > 0 : "pixelPerCm must be positive";

		width_ncell = w_ncell;
		height_ncell = h_ncell;
		torusOnXaxis = torusOnX;
		torusOnYaxis = torusOnY;
		this.cmPerCell = cmPerCell;
		this.pixelPerCm = pixelPerCm;

		width_cm = w_ncell * cmPerCell;
		height_cm = h_ncell * cmPerCell;

		assert (getWidth_ncell() * cmPerCell == getWidth_cm()) : "cmWidth and cellWidth should match";
		assert (getHeight_ncell() * cmPerCell == getHeight_cm()) : "cmWidth and cellWidth should match";

		isu = new ISU(this);
		grid = new Grid(this);
		getIsu().set(getGrid());

	}

	/**
	 * @implNote Width and height parameters might get modified to enforce
	 *           (width_ncell * cmPerCell == width_cm)
	 */
	protected Game(double w_cm, double h_cm, boolean torusOnX, boolean torusOnY, double cmPerCell, int pixelPerCm) {
		assert w_cm > 0 : "Width must be positive";
		assert h_cm > 0 : "Height must be positive";
		assert cmPerCell > 0 : "cmPerCell must be positive";
		assert pixelPerCm > 0 : "pixelPerCm must be positive";

		torusOnXaxis = torusOnX;
		torusOnYaxis = torusOnY;
		this.cmPerCell = cmPerCell;
		this.pixelPerCm = pixelPerCm;

		width_ncell = (int) Math.round(w_cm / cmPerCell);
		height_ncell = (int) Math.round(h_cm / cmPerCell);

		width_cm = getWidth_ncell() * cmPerCell;
		height_cm = getHeight_ncell() * cmPerCell;

		assert Math.abs(getWidth_ncell() * cmPerCell - getWidth_cm()) < ISU.epsilon;
		assert Math.abs(getHeight_ncell() * cmPerCell - getHeight_cm()) < ISU.epsilon;

		isu = new ISU(this);
		grid = new Grid(this);
		getIsu().set(getGrid());
	}

	// GETTER

	public Game game() {
		return this;
	}

	/** @return the world gravity (0 = none). See {@link #gravity}. */
	public double gravity() {
		return gravity;
	}

	/** Set the world gravity; 0 disables it. */
	public void setGravity(double gravity) {
		this.gravity = gravity;
	}

	public boolean torusOnX() {
		return torusOnXaxis;
	}

	public boolean torusOnY() {
		return torusOnYaxis;
	}

	/**
	 * @return the cmPerCell
	 */
	public double getCmPerCell() {
		return cmPerCell;
	}

	/**
	 * @return the pixelPerCm
	 */
	public int getPixelPerCm() {
		return pixelPerCm;
	}

	/**
	 * @return the grid
	 */
	public Grid getGrid() {
		return grid;
	}

	// SHOW

	/**
	 * @return the isu
	 */
	public ISU getIsu() {
		return isu;
	}

	public void show(PrintStream ps) {
		double w_cm = getWidth_cm();
		double h_cm = getHeight_cm();
		int ppc = getPixelPerCm();
		Grid g = getGrid();
		ISU i = getIsu();
		ps.println("=== Game State Debug ===");
		ps.println("Class: " + this.getClass().getSimpleName());
		ps.println("Grid dimensions: " + getWidth_ncell() + " x " + getHeight_ncell() + " cells");
		ps.println("Real dimensions: " + String.format("%.2f", w_cm) + " x " + String.format("%.2f", h_cm) + " cm");
		ps.println("Scale: " + getCmPerCell() + " cm/cell, " + ppc + " pixels/cm");
		ps.println("Torus: X=" + torusOnXaxis + ", Y=" + torusOnYaxis);
		ps.println("Pixel dimensions: " + (w_cm * ppc) + " x " + (h_cm * ppc) + " pixels");
		ps.println("Coordinate Systems:");
		ps.println("  Grid: " + (g != null ? g.getClass().getSimpleName() : "null"));
		ps.println("  ISU: " + (i != null ? i.getClass().getSimpleName() : "null"));
		ps.println("=======================");
	}

	/**
	 * @return the width_cm
	 */
	public double getWidth_cm() {
		return width_cm;
	}

	/**
	 * @return the height_cm
	 */
	public double getHeight_cm() {
		return height_cm;
	}

	/**
	 * @return the width_ncell
	 */
	public int getWidth_ncell() {
		return width_ncell;
	}

	/**
	 * @return the height_ncell
	 */
	public int getHeight_ncell() {
		return height_ncell;
	}
}
