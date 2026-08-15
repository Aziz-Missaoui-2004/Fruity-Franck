package engine.debug;

// == DEBUG ==

/**
 * Central, opt-in debug configuration for the engine.
 *
 * <p>
 * One instance is created by the game (e.g. in its {@code main}) and injected
 * into the pieces that own the relevant loops: the motion {@code Ticker}, the
 * {@code Painter}, and the {@code View}. Every flag defaults to {@code false},
 * so with nothing enabled — or with no {@code Debug} injected at all.
 *
 * <p>
 * Enable features through the setters, e.g.:
 *
 * <pre>
 * Debug debug = new Debug();
 * debug.setShowFps(true);
 * debug.setShowHitboxes(true);
 * </pre>
 */
public final class Debug {

	// METRIC FLAGS

	private volatile boolean showTickStats = false;
	private volatile boolean showPaintStats = false;
	private volatile boolean showFps = false;

	// OVERLAY FLAGS

	private volatile boolean showHitboxes = false;
	private volatile boolean showStates = false;
	private volatile boolean showEntityNames = false;

	// METRICS (rolling window)

	private final Stats tickStats = new Stats();
	private final Stats paintStats = new Stats();
	private final Stats fpsStats = new Stats();

	/**
	 * Pixels-per-cm fallback used to map world coordinates to screen when a
	 * {@code Camera} is absent (the legacy absolute-pixel render path). 0 means
	 * "unset": with no camera and no ppc, world-space overlays are skipped. Set it
	 * from {@code game.getPixelPerCm()} if your game renders without a camera.
	 */
	private volatile double pixelPerCm = 0;

	// FLAG GETTERS / SETTERS

	public boolean showTickStats() {
		return showTickStats;
	}

	public void setShowTickStats(boolean on) {
		this.showTickStats = on;
		if (!on)
			tickStats.reset();
	}

	public boolean showPaintStats() {
		return showPaintStats;
	}

	public void setShowPaintStats(boolean on) {
		this.showPaintStats = on;
		if (!on)
			paintStats.reset();
	}

	public boolean showFps() {
		return showFps;
	}

	public void setShowFps(boolean on) {
		this.showFps = on;
		if (!on)
			fpsStats.reset();
	}

	public boolean showHitboxes() {
		return showHitboxes;
	}

	public void setShowHitboxes(boolean on) {
		this.showHitboxes = on;
	}

	public boolean showStates() {
		return showStates;
	}

	public void setShowStates(boolean on) {
		this.showStates = on;
	}

	public boolean showEntityNames() {
		return showEntityNames;
	}

	public void setShowEntityNames(boolean on) {
		this.showEntityNames = on;
	}

	public double pixelPerCm() {
		return pixelPerCm;
	}

	public void setPixelPerCm(double ppc) {
		this.pixelPerCm = ppc;
	}

	// METRIC ACCESSORS

	public Stats tickStats() {
		return tickStats;
	}

	public Stats paintStats() {
		return paintStats;
	}

	public Stats fpsStats() {
		return fpsStats;
	}

	// CONVENIENCE

	/** @return true if any text HUD (tick/paint/fps) should be drawn. */
	public boolean anyHud() {
		return showTickStats || showPaintStats || showFps;
	}

	/** @return true if any per-entity world overlay should be drawn. */
	public boolean anyOverlay() {
		return showHitboxes || showStates;
	}
}
