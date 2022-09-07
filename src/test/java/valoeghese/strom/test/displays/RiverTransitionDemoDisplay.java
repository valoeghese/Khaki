package valoeghese.strom.test.displays;

import valoeghese.strom.utils.Maths;

public class RiverTransitionDemoDisplay implements Display {
	private int riverHeight;
	private int terrainHeight;
	private static final double riverX = 0;

	// should this change based on height diff?
	// decrease when smaller diff as it will be similar enough, or decrease when approaching transition, as it will be switching to covering, or both
	private static final double interpolateRadius = 48.0;
	private static final double finalInterpolateHeightDiff = 15.0;
	private static final double initialCoverHeightDiff = 20.0;

	private static final boolean cutoutRiver = true;

	private double getHeight(double x) {
		// strategy 1: when riverHeight ~= terrain height (or riverHeight > terrain height), linearly interpolate down to river height
		// strategy 2: when riverHeight << terrain height, do no interpolation. Instead, the river will carve it's own path under terrain
		// riverHeight >> terrain height should rarely happen
		// this would probably be better to pick between based on the terrain height specifically at the nearest river point, but whatever.
		double distToRiver = Math.abs(x - riverX);
		// the bigger this is, the higher the terrain is than the river
		double heightDiff = terrainHeight - riverHeight;

		double strategy1 = Maths.clampMap(distToRiver, 0, interpolateRadius, riverHeight, terrainHeight);
		double strategy2 = terrainHeight;

		// this could be simplified into clampMap, however this is minutely faster
		// also conveys what's happening a lot better

		// case 1, strategy 1
		if (heightDiff <= finalInterpolateHeightDiff) return strategy1;
		// case 2, strategy 2
		if (heightDiff >= initialCoverHeightDiff) return strategy2;

		// case 3, between strategy 1 and strategy 2, interpolate
		return Maths.map(heightDiff, finalInterpolateHeightDiff, initialCoverHeightDiff, strategy1, strategy2);
	}

	@Override
	public int getColour(int x, int y) {
		double xx = x;//(double) x * 0.1;
		double yy = y;//(double) y * 0.1;

		if (cutoutRiver) {
			double dx = xx - riverX;
			double dy = yy - riverHeight;
			dy *= 1.5; // squished y

			if (dx * dx + dy * dy < 10 * 10) {
				return yy < 0 ? Colours.SEA_BLUE : Colours.WHITE;
			}
		}

		return yy < this.getHeight(xx) ? Colours.BLACK : (yy < 0 ? Colours.SEA_BLUE : Colours.WHITE);
	}

	@Override
	public void modifyView(int direction) {
		this.terrainHeight += direction;
	}
}
