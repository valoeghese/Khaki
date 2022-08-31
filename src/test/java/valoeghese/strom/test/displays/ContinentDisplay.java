package valoeghese.strom.test.displays;

import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.utils.ContinentData;
import valoeghese.strom.utils.Maths;
import valoeghese.strom.utils.Point;

public class ContinentDisplay implements Display {
	public ContinentDisplay(TerrainGenerator generator) {
		this.generator = generator;

		long t = System.currentTimeMillis();
		this.pregeneratedData = this.generator.pregenerateContinentData(Point.ORIGIN);
		t = System.currentTimeMillis() - t;

		System.out.printf("Pregenerated Continent Data in %dms\n", t);
	}

	private final TerrainGenerator generator;
	private final ContinentData pregeneratedData;
	private int viewMode = 0;

	@Override
	public int getColour(int x, int y) {
		double height = (this.viewMode & 0x2) == 0 ? this.generator.sampleContinentBase(this.pregeneratedData, x, y) : this.generator._testContinentBase(this.pregeneratedData, x, y);

		if ((this.viewMode & 0x1) == 1) {
			return height < 0 ? Maths.rgb(0, 0, 200) : Maths.rgb(0, (int) Maths.clampMap(height, 0, 256, 128, 255), 0);
		}
		else {
			return Maths.grey(Maths.clampMap(height, -128, 256, 0, 1));
		}
	}

	@Override
	public void modifyView(int direction) {
		this.viewMode = (this.viewMode + direction) & 0x3;
	}
}
