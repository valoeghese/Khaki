package valoeghese.strom.test.displays;

import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.utils.Maths;
import valoeghese.strom.utils.Point;

public class BaseDisplay implements Display {
	public BaseDisplay(TerrainGenerator generator) {
		this.generator = generator;
	}

	private final TerrainGenerator generator;
	private boolean landSea = false;

	@Override
	public int getColour(int x, int y) {
		double height = this.generator.sampleContinentBase(Point.ORIGIN, x, y);

		if (this.landSea) {
			return height < 0 ? Maths.rgb(0, 0, 200) : Maths.rgb(0, (int) Maths.clampMap(height, 0, 256, 128, 255), 0);
		}
		else {
			return Maths.grey(Maths.clampMap(height, -128, 256, 0, 1));
		}
	}

	@Override
	public void modifyView() {
		this.landSea = !this.landSea;
	}
}
