package valoeghese.strom.test.displays;

import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.utils.Maths;

public class RiverTransitionDemoDisplay implements Display {
	public RiverTransitionDemoDisplay() {
	}

	private int riverHeight = 0;
	private int terrainHeight;
	private static final double riverX = 0;

	private static final boolean cutoutRiver = true;
	// decrease radius based on height diff appoaching transition, so as not to disturb much in a near-cover position

	@Override
	public int getColour(int x, int y) {
		if (cutoutRiver) {
			double dx = x - riverX;
			double dy = y - riverHeight;
			dy *= 1.5; // squished y

			if (dx * dx + dy * dy < 10 * 10) {
				return y < 0 ? Colours.SEA_BLUE : Colours.WHITE;
			}
		}

		return y < TerrainGenerator.adjustTerrainHeight(Math.abs(x - riverX), terrainHeight, riverHeight) ? Colours.BLACK : (y < 0 ? Colours.SEA_BLUE : Colours.WHITE);
	}

	@Override
	public void modifyView(int direction) {
		this.terrainHeight += direction;
	}
}
