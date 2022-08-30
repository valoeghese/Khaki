package valoeghese.strom.test.displays;

import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.test.ViewChunk;
import valoeghese.strom.test.IntPoint;

import java.util.Random;

public class VoronoiDisplay implements Display {
	public VoronoiDisplay(TerrainGenerator generator) {
		this.generator = generator;
	}

	private final TerrainGenerator generator;
	private boolean raw = false;

	@Override
	public ViewChunk genChunk(IntPoint point) {
		ViewChunk chunk = new ViewChunk(point.x(), point.y());

		for (int xo = 0; xo < ViewChunk.CHUNK_SIZE; xo++) {
			for (int yo = 0; yo < ViewChunk.CHUNK_SIZE; yo++) {
				chunk.set(xo, yo, this.generator._testVoronoiPoints(chunk.startX + xo, chunk.startY + yo, this.raw));
			}
		}

		return chunk;
	}

	@Override
	public void modifyView() {
		this.raw = !this.raw;
	}
}
