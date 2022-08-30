package valoeghese.strom.test.displays;

import valoeghese.strom.test.ViewChunk;
import valoeghese.strom.test.IntPoint;

// A display for viewing worldgen
public interface Display {
	ViewChunk genChunk(IntPoint point);

	/**
	 * Called when a user hits the hotkey (space) for getting a new variant of the view.
	 * Use for implementing multiple variants of one display in a single display.
	 */
	default void modifyView() {
	}
}
