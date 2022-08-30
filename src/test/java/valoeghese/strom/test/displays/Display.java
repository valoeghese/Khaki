package valoeghese.strom.test.displays;

import valoeghese.strom.test.ViewChunk;
import valoeghese.strom.test.IntPoint;

// A display for viewing worldgen
public interface Display {
	ViewChunk genChunk(IntPoint point);
}
