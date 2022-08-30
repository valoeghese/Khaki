package valoeghese.strom;

import valoeghese.strom.utils.LloydVoronoi;
import valoeghese.strom.utils.Maths;
import valoeghese.strom.utils.Point;
import valoeghese.strom.utils.Voronoi;

public class TerrainGenerator {
	// settings. change these.
	public int continentDiameter;
	public int riverInterpolationSteps;

	public TerrainGenerator(long seed) {
		this.seed = seed;

		// initialise generators
		//this.voronoi = new LloydVoronoi(123, 0.33);
		this.voronoi = new Voronoi(123, 0.6);
	}

	// internal stuff
	private final long seed;
	private final Voronoi voronoi;

	// inputs in scaled coordinate landscape
	public int _testVoronoiPoints(int x, int y, boolean raw) {
		// continent diminished diameter
		double cDimDiameter = this.continentDiameter >> DIMINISHED_SCALE_SHIFT;

		// axes for every 1000 blocks
		if (x % (1000 >> DIMINISHED_SCALE_SHIFT) == 0) return 0;
		if (y % (1000 >> DIMINISHED_SCALE_SHIFT) == 0) return 0;

		final double voronoiSize = cDimDiameter * 1.5; // extra area for oceans

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
		if (point.squaredDist(x, y) < cDimRad * cDimRad) {
			return Maths.rgb(20, 200, 0);
		}
		else {
			return Maths.rgb(0, 60, 120);
		}
	}

	// if a continent is gonna be around ~5000 blocks in radius, and we probably only want ~200x200, shift 4
	// 4096/(2^4) = 256
	private static final int DIMINISHED_SCALE_SHIFT = 4;
}
