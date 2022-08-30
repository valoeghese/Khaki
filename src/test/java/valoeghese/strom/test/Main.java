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

		Display display = new VoronoiDisplay(generator);

		PanelTest inst = new Main(display).scale(2).size(800);
		inst.addKeyListener(
				new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						// special actions here
						if (e.getKeyChar() == ' ') {
							mode = (mode + 1) & 1;
							inst.redraw(false);
						}
					}
				});

		inst.start("Khaki 2 / Strom: " + System.currentTimeMillis());
	}

	Main(Display display) {
		this.display = display;
	}

	private final Display display;
	private static int mode = 0;

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