package valoeghese.khaki.test.displays;

import valoeghese.khaki.TerrainGenerator;
import valoeghese.khaki.utils.Maths;

public class CombinedContinentDisplay extends ContinentDisplay {
	public CombinedContinentDisplay(TerrainGenerator generator) {
		super(generator, null);
	}

	private static double[] reuseable = new double[3];

	@Override
	public int getColour(int x, int y) {
		x *= 2;
		y *= 2;

		boolean iAmCoveredUp = false;

		this.generator.sampleHeight(x, y, reuseable);
		double height = reuseable[0];

		// if close to river & not covered just make it blue
		if ((this.viewMode & 0x1) == 1 && reuseable[2] < 4) {
			iAmCoveredUp = reuseable[1] > height;
			height = -64;
		}

		if ((this.viewMode & 0x1) == 1) {
			if (iAmCoveredUp) return Colours.SKY;
			if (Math.floor(height) == 0) return Colours.SANDY;
			return height < 0 ? Colours.SEA_BLUE : Maths.rgb(0, (int) Maths.clampMap(height, 0, 256, 128, 255), 0);
		}
		else {
			return Maths.grey(Maths.clampMap(height, -128, 256, 0, 1));
		}
	}
}
