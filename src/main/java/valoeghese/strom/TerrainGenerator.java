package valoeghese.strom;

import valoeghese.strom.utils.Maths;
import valoeghese.strom.utils.Noise;
import valoeghese.strom.utils.Point;
import valoeghese.strom.utils.Voronoi;

import java.util.Random;

public class TerrainGenerator {
	// settings. change these.
	public int continentDiameter;
	public int riverInterpolationSteps;

	public TerrainGenerator(long seed) {
		this.seed = seed;

		// initialise generators
		//this.voronoi = new LloydVoronoi(seed, 0.33);
		this.voronoi = new Voronoi(seed, 0.6);
		this.noise = new Noise(new Random(seed));
	}

	// internal stuff
	private final long seed;
	private final Voronoi voronoi;
	private final Noise noise;

	// calculates the height at a position, excluding continental features
	// inputs in block coordinate landscape
	// output in blocks between -128 and 256
	// treat sea level = 0 (this is why we use -128 to 256 rather than -64 to 320)
	public double sampleContinentBase(Point centre, int x, int y) {
		// base heightmap is done via radial + noise

		// scale that dist of radius = 1, then invert and clamp
		double radial = 1.0 - centre.distance(x, y) / (continentDiameter * 0.5);

		// manipulate, sum, clamp
		return Math.max(-64, 0
				+ 60 * (radial - 0.2)
				+ 30 * this.noise.sample(x * BASE_DISTORT_FREQUENCY, y * BASE_DISTORT_FREQUENCY)
				+ 30 * radial * Math.max(0, this.noise.sample(x * BASE_HILLS_FREQUENCY, y * BASE_HILLS_FREQUENCY)) // hills
		);
	}

	// inputs in diminished coordinate landscape
	public int _testVoronoiPoints(int x, int y, boolean raw, boolean innerLines) {
		// continent diminished diameter
		double cDimDiameter = this.continentDiameter >> DIMINISHED_SCALE_SHIFT;

		// axes for every ~1000 blocks
		if (x % (5 * (200 >> DIMINISHED_SCALE_SHIFT)) == 0) return 0;
		if (y % (5 * (200 >> DIMINISHED_SCALE_SHIFT)) == 0) return 0;

		if (innerLines) {
			// axes for every ~200 blocks
			if (x % (200 >> DIMINISHED_SCALE_SHIFT) == 0) return Maths.rgb(100, 100, 100);
			if (y % (200 >> DIMINISHED_SCALE_SHIFT) == 0) return Maths.rgb(100, 100, 100);
		}

		final double voronoiSize = cDimDiameter * 1.6; // extra area for oceans

		// voronoi regions
		// shift into voronoi space
		double voronoiX = (double) x / voronoiSize;
		double voronoiY = (double) y / voronoiSize;

		Point point = this.voronoi.sampleC(voronoiX, voronoiY).mul(voronoiSize); // shift out of voronoi space

		// raw, just use voronoi colours
		if (raw) {
			int value = point.getValue();
			boolean origin = (value == Point.ORIGIN.getValue() && (int) voronoiX == 0 && (int) voronoiY == 0);
			return point.squaredDist(x, y) < cDimDiameter ? (origin ? Maths.rgb(255, 0, 0) : 0) : value;
		}

		double cDimRad = cDimDiameter * 0.5; // radius

		// processed, show land and sea areas
		double sqrDist = point.squaredDist(x, y);

		if (sqrDist < cDimRad * cDimRad) {
			return Maths.rgb(20, 200, 0);
		}
		if (sqrDist < Maths.sqr(cDimRad + (100 >> DIMINISHED_SCALE_SHIFT))) {
			return Maths.rgb(0, 160, 160);
		}
		else {
			return Maths.rgb(0, 60, 120);
		}
	}

	public static final double BASE_DISTORT_FREQUENCY = 1.0 / 850.0;
	public static final double BASE_HILLS_FREQUENCY = 1.0 / 300.0;

	// if a continent is gonna be around ~5000 blocks in radius, and we probably only want ~200x200, shift 4
	// 4096/(2^4) = 256
	public static final int DIMINISHED_SCALE_SHIFT = 4;
}
