package valoeghese.strom.test;

import valoeghese.strom.ContinentData;
import valoeghese.strom.TerrainGenerator;
import valoeghese.strom.test.displays.BaseContinentDisplay;
import valoeghese.strom.test.displays.Display;
import valoeghese.strom.test.displays.RiverContinentDisplay;
import valoeghese.strom.test.displays.VoronoiDisplay;
import valoeghese.strom.utils.Point;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// World Gen Visualiser stolen from Jerraria World Gen, another project I work on
// The panel test code from which is stolen from code I have written even earlier
public class Main extends PanelTest {
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// last seed I had to fix: 2333285187735730582L
		// 6328360064685290253 is a bit funny ...
		// last last edited merging with: -6731418085665489146 : Solution: was using Y (a horizontal axis in the generator) instead of Value (which stores height)
		// last edited merging with: 4310129288462693751 to add backwards merging, then smoothing, and fix an issue with rivers both redirecting and merging into another river creating loops
		long seed = new Random().nextLong();
		System.out.println("Using Seed: " + seed);

		// create worldgen
		TerrainGenerator generator = new TerrainGenerator(seed);
		generator.continentDiameter = 4000;
		generator.riverInterpolationSteps = 10;
		generator.mountainsPerRange = 11;
		generator.riverStep = 16;
		generator.riverCount = 10;
		generator.mergeThreshold = 12.0;
		generator.warn = System.err::println;

		// Pregenerate the central continent data
		long t = System.currentTimeMillis();
		ContinentData pregeneratedData = generator.pregenerateContinentData(Point.ORIGIN);
		t = System.currentTimeMillis() - t;
		System.out.printf("Pregenerated Continent Data in %dms\n", t);

		Display displays[] = {
				new VoronoiDisplay(generator),
				new BaseContinentDisplay(generator, pregeneratedData),
				new RiverContinentDisplay(generator, pregeneratedData)
		};

		window = (Main) new Main().scale(2).size(800);
		window.selected = displays.length;
		window.display = displays[window.selected - 1];

		window.addKeyListener(
				new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						int code = e.getKeyCode();

						// special actions here
						if (code == 37) {
							// mode special action
							window.display.modifyView(-1);
							window.redraw(false);
						}
						else if (code == 39 || e.getKeyChar() == ' ') {
							// mode special action
							window.display.modifyView(1);
							window.redraw(false);
						}
						else {
							// mode type
							int chnumV = Character.getNumericValue(e.getKeyChar());
							if (chnumV == 0) chnumV = 10; // because of arrangement on keyboard

							if (chnumV > 0 && chnumV <= displays.length && chnumV != window.selected) {
								window.selected = chnumV;
								window.display = displays[chnumV - 1];
								window.redraw(false);
							}
						}
					}
				});

		window.start("Khaki 2 / Strom: " + seed + " @ " + DateTimeFormatter.ofPattern("dd/MM HH:mm:ss").format(LocalDateTime.now()));
	}

	private Display display;
	private int selected = 0;

	private Map<IntPoint, ViewChunk> chunks = new HashMap<>();

	@Override
	public void redraw(boolean reuse) {
		if (reuse) {
			for (ViewChunk chunk : this.chunks.values()) {
				chunk.shouldDestroy = true;
			}
		}
		else {
			this.chunks.clear();
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
		x >>= 2;
		y = -(y >> 2);

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

	public static Main window;
}