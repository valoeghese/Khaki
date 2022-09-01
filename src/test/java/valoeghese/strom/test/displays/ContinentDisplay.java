package valoeghese.strom.test.displays;

import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.ContinentData;
import valoeghese.strom.utils.Point;

public abstract class ContinentDisplay implements Display {
	public ContinentDisplay(TerrainGenerator generator) {
		this.generator = generator;

		long t = System.currentTimeMillis();
		this.pregeneratedData = this.generator.pregenerateContinentData(Point.ORIGIN);
		t = System.currentTimeMillis() - t;

		System.out.printf("Pregenerated Continent Data in %dms\n", t);
	}

	protected final TerrainGenerator generator;
	protected final ContinentData pregeneratedData;
	protected int viewMode = 0;
	protected int viewModes = 1;

	@Override
	public void modifyView(int direction) {
		this.viewMode = (this.viewMode + direction) & this.viewModes;
	}
}
