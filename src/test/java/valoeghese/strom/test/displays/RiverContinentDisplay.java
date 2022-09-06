package valoeghese.strom.test.displays;

import valoeghese.strom.ContinentData;
import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.utils.Maths;

public class RiverContinentDisplay extends ContinentDisplay {
	public RiverContinentDisplay(TerrainGenerator generator, ContinentData pregeneratedData) {
		super(generator, pregeneratedData);
	}

	@Override
	public int getColour(int x, int y) {
		//if (Maths.isPosApproximatelyAt(x, y, -373, -261, 4)) return Maths.rgb(200, 0, 50);
		double height = this.generator._testContinentRiver(this.pregeneratedData, x, y);

		if ((this.viewMode & 0x1) == 1) {
			if (Math.floor(height) == 0) return Colours.SANDY;
			return height < 0 ? Colours.SEA_BLUE : Maths.rgb(0, (int) Maths.clampMap(height, 0, 256, 128, 255), 0);
		}
		else {
			return Maths.grey(Maths.clampMap(height, -128, 256, 0, 1));
		}
	}
}
