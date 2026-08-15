package engine.graphics;

// == MINIMAP ==

/**
 * A fixed corner panel that shows the whole world scaled down. The engine
 * provides the world&rarr;panel mapping and the panel rectangle; <i>what</i> to
 * draw (walls, entity dots, the viewport outline) is the game's job, since only
 * the game knows what its entities look like.
 */
public class Minimap {

	private final double worldW, worldH; // cm
	private final int panelX, panelY, panelW, panelH; // px (screen-space)
	private final double sx, sy; // cm -> panel px

	public Minimap(double worldWcm, double worldHcm, int panelX, int panelY, int panelW, int panelH) {
		this.worldW = worldWcm;
		this.worldH = worldHcm;
		this.panelX = panelX;
		this.panelY = panelY;
		this.panelW = panelW;
		this.panelH = panelH;
		this.sx = panelW / worldWcm;
		this.sy = panelH / worldHcm;
	}

	/**
	 * @return the panel pixel {@code {x, y}} for a world point (cm), wrapped into
	 *         the world.
	 */
	public int[] worldToPanel(double cmX, double cmY) {
		double wx = ((cmX % worldW) + worldW) % worldW;
		double wy = ((cmY % worldH) + worldH) % worldH;
		return new int[] { panelX + (int) Math.round(wx * sx), panelY + (int) Math.round(wy * sy) };
	}

	public int panelX() {
		return panelX;
	}

	public int panelY() {
		return panelY;
	}

	public int panelW() {
		return panelW;
	}

	public int panelH() {
		return panelH;
	}

	public double scaleX() {
		return sx;
	}

	public double scaleY() {
		return sy;
	}
}
