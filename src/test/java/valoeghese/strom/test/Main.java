package valoeghese.strom.test;

import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.test.displays.Display;
import valoeghese.strom.test.displays.VoronoiDisplay;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// World Gen Visualiser stolen from Jerraria World Gen, another project I work on
// The panel test code from which is stolen from code I have written even earlier
public class Main extends PanelTest {
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		// create worldgen
		TerrainGenerator generator = new TerrainGenerator(new Random().nextLong());
		generator.continentRadius = 1000;
		generator.riverInterpolationSteps = 10;

		Display displays[] = {
				new VoronoiDisplay(generator)
		};

		Main window = (Main) new Main().scale(2).size(800);
		window.selected = displays.length;
		window.display = displays[window.selected - 1];

		window.addKeyListener(
				new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						// special actions here
						int chnumV = Character.getNumericValue(e.getKeyChar());
						if (chnumV == 0) chnumV = 10; // because of arrangement on keyboard

						if (chnumV > 0 && chnumV <= displays.length && chnumV != window.selected) {
							window.selected = chnumV;
							window.display = displays[chnumV - 1];
							window.redraw(false);
						}
					}
				});

		window.start("Khaki 2 / Strom: " + System.currentTimeMillis());
	}

	private Display display;
	private int selected = 0;

	private Map<IntPoint, ViewChunk> chunks = new HashMap<>();

	@Override
	public void redraw(boolean reuse) {
		for (ViewChunk chunk : this.chunks.values()) {
			chunk.shouldDestroy = true;
		}

		super.redraw(reuse);

		for (IntPoint pos : List.copyOf(this.chunks.keySet())) {
			if (this.chunks.get(pos).shouldDestroy) {
				this.chunks.remove(pos);
			}
		}
	}

	@Override
	protected int getColour(int x, int y) {
		// adjust positions
		x >>= 3;
		y = -(y >> 3);

		IntPoint key = new IntPoint(x >> ViewChunk.CHUNK_SHIFT, y >> ViewChunk.CHUNK_SHIFT);

		ViewChunk chunk = this.chunks.get(key);

		if (chunk == null) {
			synchronized (this.display) {
				// computeIfAbsent since could be added by another thread
				chunk = this.chunks.computeIfAbsent(key, this.display::genChunk);
			}
		}

		chunk.shouldDestroy = false;
		return chunk.get(x & ViewChunk.CHUNK_MOD, y & ViewChunk.CHUNK_MOD);
	}
}