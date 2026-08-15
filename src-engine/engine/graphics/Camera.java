package engine.graphics;

// == CAMERA ==

/**
 * Maps world coordinates (cm) to viewport pixels for a world that is larger
 * than the window. The camera follows a target with a <i>dead-zone</i> (it only
 * scrolls once the target leaves a central box) and eases toward it (lerp),
 * giving the "centred on the player with elasticity" feel the spec asks for.
 *
 * <p>
 * Torus-aware: {@link #worldToScreen} places a point by its shortest wrapped
 * offset from the camera centre, so the seam is invisible and entities across
 * the wrap render on the correct side. On a non-torus axis the centre is
 * clamped so the viewport never shows past the world edge.
 */
public class Camera {

	private final double worldW, worldH; // cm
	private final boolean torusX, torusY;
	private final int viewW, viewH; // px
	private final double ppc; // pixels per cm

	private double camX, camY; // camera centre, cm
	private double tgtX, tgtY; // follow target, cm

	private double deadZoneHalfX, deadZoneHalfY; // cm
	private int windowOffsetX = 0; // px: left crop of the centered display window within the wider render
	private double lerp = 0.15;
	
	private boolean forceCentering = false;

	public Camera(double worldWcm, double worldHcm, boolean torusX, boolean torusY, int viewWpx, int viewHpx,
			double pixelPerCm) {
		this.worldW = worldWcm;
		this.worldH = worldHcm;
		this.torusX = torusX;
		this.torusY = torusY;
		this.viewW = viewWpx;
		this.viewH = viewHpx;
		this.ppc = pixelPerCm;
		this.camX = worldWcm / 2;
		this.camY = worldHcm / 2;
		// Dead-zone ~ a third of the viewport in each axis.
		this.deadZoneHalfX = (viewWpx / ppc) / 6.0;
		this.deadZoneHalfY = (viewHpx / ppc) / 6.0;
	}

	/** Set the elasticity (0 = frozen, 1 = snap instantly). */
	public void setLerp(double lerp) {
		this.lerp = lerp;
	}

	public void setDeadZoneHalfX(double deadZoneHalfX) {
		this.deadZoneHalfX = deadZoneHalfX;
	}

	/** Display only a centered window of {@code windowWpx} within the rendered viewport; world points then map into that window. */
	public void setWindowWidth(int windowWpx) {
		this.windowOffsetX = Math.max(0, (viewW - windowWpx) / 2);
	}

	public void setTarget(double cmX, double cmY) {
		this.tgtX = cmX;
		this.tgtY = cmY;
	}
	
	public void smoothCenterOn(double cmX, double cmY) {
	    this.tgtX = cmX;
	    this.tgtY = cmY;
	    this.forceCentering = true;
	}

	/**
	 * Ease the centre toward the target by the part of the offset beyond the
	 * dead-zone.
	 */
	public void update() {
	    double dx = signed(camX, tgtX, worldW, torusX);
	    double dy = signed(camY, tgtY, worldH, torusY);

	    if (forceCentering) {
	        camX = settle(camX + dx * lerp, worldW, torusX, viewW);
	        camY = settle(camY + dy * lerp, worldH, torusY, viewH);

	        double eps = 0.05; // cm, seuil très petit
	        if (Math.abs(dx) < eps && Math.abs(dy) < eps) {
	            camX = settle(tgtX, worldW, torusX, viewW);
	            camY = settle(tgtY, worldH, torusY, viewH);
	            forceCentering = false;
	        }

	        return;
	    }

	    camX = settle(camX + excess(dx, deadZoneHalfX) * lerp, worldW, torusX, viewW);
	    camY = settle(camY + excess(dy, deadZoneHalfY) * lerp, worldH, torusY, viewH);
	}

	/** @return the screen pixel {@code {sx, sy}} for a world point (cm), in display-window space. */
	public int[] worldToScreen(double cmX, double cmY) {
		double dx = signed(camX, cmX, worldW, torusX);
		double dy = signed(camY, cmY, worldH, torusY);
		return new int[] { (int) Math.round(viewW / 2.0 + dx * ppc) - windowOffsetX,
				(int) Math.round(viewH / 2.0 + dy * ppc) };
	}

	/**
	 * @return whether a world point is within the viewport (plus a cell margin).
	 */
	public boolean isVisible(double cmX, double cmY, double marginCm) {
		double dx = Math.abs(signed(camX, cmX, worldW, torusX));
		double dy = Math.abs(signed(camY, cmY, worldH, torusY));
		return dx <= viewW / 2.0 / ppc + marginCm && dy <= viewH / 2.0 / ppc + marginCm;
	}

	public double centreX() {
		return camX;
	}

	public double centreY() {
		return camY;
	}

	/** @return pixels per cm — the scale this camera maps world distances with. */
	public double pixelPerCm() {
		return ppc;
	}

	public double viewWidthCm() {
		return viewW / ppc;
	}

	public double viewHeightCm() {
		return viewH / ppc;
	}

	// HELPERS

	/**
	 * Shortest signed delta from {@code from} to {@code to} (torus-wrapped if
	 * asked).
	 */
	private static double signed(double from, double to, double size, boolean torus) {
		double d = to - from;
		if (torus) {
			double half = size / 2;
			if (d > half)
				d -= size;
			else if (d < -half)
				d += size;
		}
		return d;
	}

	/** The part of {@code d} beyond ±{@code half} (0 inside the dead-zone). */
	private static double excess(double d, double half) {
		if (d > half)
			return d - half;
		if (d < -half)
			return d + half;
		return 0;
	}

	/**
	 * Wrap (torus) or clamp (so the viewport stays inside the world) the centre.
	 */
	private double settle(double v, double size, boolean torus, int viewPx) {
		if (torus)
			return ((v % size) + size) % size;
		double halfView = viewPx / 2.0 / ppc;
		if (size <= 2 * halfView)
			return size / 2; // world narrower than the viewport: keep it centred
		return Math.max(halfView, Math.min(size - halfView, v));
	}
}
