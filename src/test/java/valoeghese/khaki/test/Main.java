package valoeghese.khaki.test;

import valoeghese.khaki.ContinentData;
import valoeghese.khaki.TerrainGenerator;
import valoeghese.khaki.test.displays.BaseContinentDisplay;
import valoeghese.khaki.test.displays.CombinedContinentDisplay;
import valoeghese.khaki.test.displays.Display;
import valoeghese.khaki.test.displays.LineBetweenDisplay;
import valoeghese.khaki.test.displays.RiverContinentDisplay;
import valoeghese.khaki.test.displays.RiverTransitionDemoDisplay;
import valoeghese.khaki.test.displays.VoronoiDisplay;
import valoeghese.khaki.utils.Point;

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
		// last seed I had to fix: 7583974426950350893
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
		//generator.debug = System.out::printf;

		// Pregenerate the central continent data
		long t = System.currentTimeMillis();
		ContinentData pregeneratedData = generator.pregenerateContinentData(Point.ORIGIN);
		t = System.currentTimeMillis() - t;
		System.out.printf("Pregenerated Continent Data in %dms\n", t);

		Display displays[] = {
				new VoronoiDisplay(generator), // 1
				new BaseContinentDisplay(generator, pregeneratedData), // 2
				new RiverContinentDisplay(generator, pregeneratedData), // 3
				new LineBetweenDisplay(), // 4
				new RiverTransitionDemoDisplay(), // 5
				new CombinedContinentDisplay(generator) // 6
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