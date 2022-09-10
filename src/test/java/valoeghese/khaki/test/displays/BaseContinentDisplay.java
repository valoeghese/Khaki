package valoeghese.khaki.test.displays;

import valoeghese.khaki.ContinentData;
import valoeghese.khaki.TerrainGenerator;
import valoeghese.khaki.utils.Maths;

public class BaseContinentDisplay extends ContinentDisplay {
	public BaseContinentDisplay(TerrainGenerator generator, ContinentData pregeneratedData) {
		super(generator, pregeneratedData);
		this.viewModes = 0x3;
	}

	@Override
	public int getColour(int x, int y) {
		if (Maths.isApproxEqu(x * x + y * y, Maths.sqr((double)generator.continentDiameter/2.0), 100 * 100)) return Colours.RED;
		double height = (this.viewMode & 0x2) == 0 ? this.generator.sampleContinentBase(this.pregeneratedData, x, y) : this.generator._testContinentBase(this.pregeneratedData, x, y);

		if ((this.viewMode & 0x1) == 1) {
			return height < 0 ? Colours.SEA_BLUE : Maths.rgb(0, (int) Maths.clampMap(height, 0, 256, 128, 255), 0);
		}
		else {
			return Maths.grey(Maths.clampMap(height, -128, 256, 0, 1));
		}
	}
}
