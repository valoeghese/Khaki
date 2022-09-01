package valoeghese.strom.test.displays;

import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.ContinentData;
import valoeghese.strom.utils.Point;

public abstract class ContinentDisplay implements Display {
	public ContinentDisplay(TerrainGenerator generator, ContinentData pregeneratedData) {
		this.generator = generator;
		this.pregeneratedData = pregeneratedData;
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
