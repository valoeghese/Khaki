package valoeghese.strom.test.displays;

import valoeghese.strom.test.Main;
import valoeghese.strom.test.ViewChunk;
import valoeghese.strom.test.IntPoint;

// A display for viewing worldgen
public interface Display {
	default ViewChunk genChunk(IntPoint point) {
		ViewChunk chunk = new ViewChunk(point.x(), point.y());

		for (int xo = 0; xo < ViewChunk.CHUNK_SIZE; xo++) {
			for (int yo = 0; yo < ViewChunk.CHUNK_SIZE; yo++) {
				chunk.set(xo, yo, this.getColour(chunk.startX + xo, chunk.startY + yo));
			}
		}

		return chunk;
	}

	int getColour(int x, int y);

	/**
	 * Called when a user hits the hotkey (space) for getting a new variant of the view.
	 * Use for implementing multiple variants of one display in a single display.
	 */
	default void modifyView() {
	}
}
