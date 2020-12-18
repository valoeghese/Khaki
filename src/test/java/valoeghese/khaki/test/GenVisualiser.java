package valoeghese.khaki.test;

import java.util.Random;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import valoeghese.khaki.KhakiNoiseGenerator;
import valoeghese.khaki.KhakiNoiseGenerator.GridShape;

// Simulator of "normal terrain" heightmap. Produces a javafx image thereof.
public final class GenVisualiser extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Pane pane = new Pane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		pane.getChildren().add(canvas);
		stage.setScene(new Scene(pane));
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);

		KhakiNoiseGenerator noiseGen = new KhakiNoiseGenerator(new Random().nextLong());

		/*stage.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			System.out.println((e.getScreenX() * SCALE) + ", " + (e.getScreenY() * SCALE));
		});*/

		try {
			drawTo(noiseGen, canvas.getGraphicsContext2D().getPixelWriter(), WIDTH, HEIGHT);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		stage.show();
	}

	private static void drawTo(KhakiNoiseGenerator noiseGen, PixelWriter writer, int width, int height) {
		for (int x = 0; x < width; ++x) {
			for (int z = 0; z < height; ++z) {
				writer.setColor(x, z, visualiseChunkRivers(x >> SCALE, z >> SCALE, noiseGen));
				//				writer.setColor(x, z, visualiseRiverDirections(x >> SCALE, z >> SCALE, noiseGen));
				//				writer.setColor(x, z, GridUtils.isNearLineBetween(928, 898, 345, -45, x, z, 4) ? Color.WHITE : Color.BLACK);
				//				writer.setColor(x, z, getColour(255 * (noiseGen.getRiverData(x >> SCALE, z >> SCALE) > 0 ? 1 : 0), noiseGen.getBaseHeight(x >> SCALE, z >> SCALE), 80));
				//writer.setColor(x, z, Color.grayRgb(70 * (noiseGen.getPositionData(x >> SCALE, z >> SCALE) & 3)));
				//				writer.setColor(x, z, Color.grayRgb(255 * (noiseGen.getRiverData(x >> SCALE, z >> SCALE) > 0 ? 1 : 0)));
			}
		}
	}

	static Color visualiseRiverDirections(int megaChunkX, int megaChunkZ, KhakiNoiseGenerator noiseGen) {
		int number = noiseGen.getRiverData(megaChunkX, megaChunkZ);

		if (number > 0) {
			int iShape = number & 0b11;

			// TEST IF SHIFTING UNNECCESARILY
			/*while ((number >>= 4) > 0) {
				iShape = number & 0b11;
			}*/

			GridShape shape = GridShape.BY_ID[iShape];
			switch (shape) {
			case ANTICLOCKWISE:
				return Color.RED;
			case LINE:
				return Color.YELLOW;
			case CLOCKWISE:
				return Color.GREEN;
			case NODE:
				return Color.WHITE;
			default:
				return Color.BLACK;
			}
		}
		return Color.gray((double) noiseGen.getBaseHeight(megaChunkX, megaChunkZ) / 256.0);
	}

	private static Color visualiseChunkRivers(int x, int z, KhakiNoiseGenerator noiseGen) {
		int red = noiseGen.checkForRivers(x, z) > 0 ? 255 : 0;
		int megaX = (x >> 4);
		int megaZ = (z >> 4);
		int green = noiseGen.getBaseHeight(megaX, megaZ);
		int blue = noiseGen.getRiverData(megaX, megaZ) > 0 ? 255 : 0;
		return Color.rgb(red, green, blue);
	}

	static Color getColour(int red, double height, double seaLevel) {
		if (height > 255) height = 255;
		else if (height < 0) height = 0;

		return height > seaLevel ? Color.rgb(red, (int) height, 0) : Color.rgb(red, 0, (int) height);
	}

	static Color getColour(double height, int beachHeightOffset, int x, int z) {
		int h = (int) height + 63;

		if (h > 204) {
			return Color.WHITE;
		} else if (h < 63) {
			return Color.rgb(10, 20, (int) map(h, 0, 63, 0, 0xED));
		} else if (h <= 63 + beachHeightOffset) {
			int c = (int) map(h, 63, 67, 0xc5, 0xf5);
			return Color.rgb(c, c, 0);
		} else if (h < 135) {
			return Color.rgb(0x32, (int) map(h, 63, 135, 0x50, 0xb0), 0x32);
		} else if (h < 153) {
			return Color.rgb((int) map(h, 135, 153, 0x6b, 0xab), (int) map(h, 135, 153, 0x25, 0x65), 0x13);
		} else {
			return Color.gray(map(h, 153, 204, 0.4, 1.0));
		}
	}

	private static double map(double value, double min, double max, double newmin, double newmax) {
		value -= min;
		value /= (max - min);
		return newmin + value * (newmax - newmin);
	}

	private static final int SCALE = 1;
	private static final int WIDTH = 1000, HEIGHT = 800;
}
