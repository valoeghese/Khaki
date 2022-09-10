package valoeghese.khaki.test.displays;

import valoeghese.khaki.TerrainGenerator;
import valoeghese.khaki.test.Main;

public class VoronoiDisplay implements Display {
	public VoronoiDisplay(TerrainGenerator generator) {
		this.generator = generator;
	}

	private final TerrainGenerator generator;
	private boolean raw = false;

	@Override
	public int getColour(int x, int y) {
		return this.generator._testVoronoiPoints(x, y, this.raw, Main.window.getScale() < 8);
	}

	@Override
	public void modifyView(int direction) {
		this.raw = !this.raw;
	}
}
