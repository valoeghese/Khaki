package valoeghese.khaki.test.displays;

import valoeghese.khaki.ContinentData;
import valoeghese.khaki.TerrainGenerator;
import valoeghese.khaki.utils.Maths;

public class RiverContinentDisplay extends ContinentDisplay {
	public RiverContinentDisplay(TerrainGenerator generator, ContinentData pregeneratedData) {
		super(generator, pregeneratedData);
		this.viewModes = 0x2 | 0x1;
	}

	private static double[] reuseable = new double[3];

	@Override
	public int getColour(int x, int y) {
		//if (Maths.isPosApproximatelyAt(x, y, -373, -261, 4)) return Maths.rgb(200, 0, 50);
		double height = 0;
		boolean iAmCoveredUp = false;

		if ((this.viewMode & 0x2) == 0) {
			this.generator.sampleContinentHeights(this.pregeneratedData, x, y, reuseable);
			height = reuseable[0];

			// if close to river & not covered just make it blue
			if ((this.viewMode & 0x1) == 1 && reuseable[2] < 4) {
				iAmCoveredUp = reuseable[1] > height;
				height = -64;
			}
		} else {
			height = this.generator._testContinentRiver(this.pregeneratedData, x, y);
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
