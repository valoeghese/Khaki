package valoeghese.khaki.test.displays;

import valoeghese.khaki.TerrainGenerator;
import valoeghese.khaki.ContinentData;

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
