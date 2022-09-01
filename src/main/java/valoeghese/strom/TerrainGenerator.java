package valoeghese.strom;

import valoeghese.strom.utils.ContinentData;
import valoeghese.strom.utils.Maths;
import valoeghese.strom.utils.Noise;
import valoeghese.strom.utils.Point;
import valoeghese.strom.utils.Voronoi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TerrainGenerator {
	// settings. change these.
	public int continentDiameter;
	public int riverInterpolationSteps;
	public int mountainsPerRange;
	public double riverStep;

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

	public double mtnTransform(double v) {
		return Math.sqrt(v); // 0.5 - 32 * Maths.pow5(Math.sqrt(v) - 0.5);
	}

	/**
	 * Calculates the height at a position, excluding rivers.
	 *
	 * Inputs in block coordinate landscape
	 * Output in blocks between -128 and 256 and treat sea level = 0 ({@code h < 0 = sea, h >= 0 = land})
	 * Of course, in minecraft, sea level will be 63. However that is cringe so I shifted it down to 0.
	 * If implementing this in your work, make sure to adjust the exact generation to and input/output space to suit your game.
	 */
	public double sampleContinentBase(ContinentData continentData, int x, int y) {
		// base heightmap is done via radial + noise + continental features

		// ======== SHAPE ==========

		// scale that dist of radius = 1, then invert and clamp
		double radial = 1.0 - continentData.centre().distance(x, y) / (continentDiameter * 0.5);

		// manipulate, sum, clamp
		double height = Maths.clamp(-64, 256, 0
				+ 60 * (radial - 0.2)
				+ 30 * this.noise.sample(x * BASE_DISTORT_FREQUENCY, y * BASE_DISTORT_FREQUENCY)
				+ 30 * radial * Math.max(0, this.noise.sample(x * BASE_HILLS_FREQUENCY, y * BASE_HILLS_FREQUENCY)) // hills
		);

		// ======== MOUNTAINS ==========

		// the TOTAL RADIUS which the mountain AFFECTS, not the radius of the "mountain feature" visually!
		double modMtnRadius = mtnTransform(750);

		// find the max mtn strength
		// todo smoother transitions between mountains
		double maxMtnStrength = 0;

		// also treat height as weighted average of how 'strong' they are
		double weightedMtnHeight = 0;
		double weightsSum = 0.0001; // epsilon, prevent division by 0

		for (Point p : continentData.mountains()) {
			double d = 1.0 - mtnTransform(p.distance(x, y)) / modMtnRadius;
			double mtnStrength = Maths.clamp(0, 1, d);

			if (mtnStrength > maxMtnStrength) {
				maxMtnStrength = mtnStrength;
			}

			// weight sum & add weighted height for weighted average blending
			weightedMtnHeight += mtnStrength * p.getValue();
			weightsSum += mtnStrength;
		}

		// max mtn strength affects terrain
		height = Maths.lerp(maxMtnStrength, height, weightedMtnHeight / weightsSum);

		return height;
	}

	public ContinentData pregenerateContinentData(Point centre) {
		final int minMtnHeight = 140;
		final int deltaMtnHeight = 255 - minMtnHeight;
		// IF CHANGING HEIGHT ALGORITHM, REPLACE ALL INSTANCES OF rand.nextInt(deltaMtnHeight) + minMtnHeight

		// random for pregen
		Random rand = new Random(centre.getValue() + this.seed);

		final int nMtns = this.mountainsPerRange;

		Point[] mountainRange = new Point[nMtns];
		double chainX = (rand.nextDouble() - 0.5) * 0.5 * this.continentDiameter + centre.getX();
		double chainY = (rand.nextDouble() - 0.5) * 0.5 * this.continentDiameter + centre.getY();

		// each point also carries, as a value, the mountain height.
		Point start = new Point(chainX, chainY, rand.nextInt(deltaMtnHeight) + minMtnHeight);

		double eDX = (rand.nextDouble() - 0.5) * 0.5 * this.continentDiameter; // endDX
		double eDY =  (rand.nextDouble() - 0.5) * 0.5 * this.continentDiameter; // endDY
		//System.out.println((Math.abs(eDX) + Math.abs(eDY)) / this.continentDiameter);

		// inb4 edX and eDy ~= 0 and the game gets stuck in a very big loop
		// sike let's harcode a fix for that
		if (Math.abs(eDX) + Math.abs(eDY) < 0.002 * this.continentDiameter) {
			//System.out.println("Applying emergency EDX offset");
			eDX += (0.5 + rand.nextDouble()) * 0.3 * this.continentDiameter;
		}

		while (Math.abs(eDX) + Math.abs(eDY) < 0.175 * this.continentDiameter) {
			//System.out.println("Amplifying Point Spread");
			eDX *= 2;
			eDY *= 2;
		}

		Point end = new Point(
				chainX + eDX,
				chainY + eDY,
				rand.nextInt(deltaMtnHeight) + minMtnHeight
		);

		mountainRange[0] = start;
		mountainRange[nMtns - 1] = end;

		for (int i = 1; i < nMtns - 1; i++) {
			mountainRange[i] = start.lerp((double) i / (double)(nMtns - 1), end).add(
					(rand.nextDouble() - 0.5) * 0.05 * this.continentDiameter,
					(rand.nextDouble() - 0.5) * 0.05 * this.continentDiameter,
					rand.nextInt(deltaMtnHeight) + minMtnHeight
			);
		}

		return this.generateRivers(new ContinentData(centre, mountainRange, new ArrayList<>()), rand);
	}

	private ContinentData generateRivers(ContinentData continentData, Random rand) {
		final int nRiversToGenerate = 3;

		for (int i = 0; i < nRiversToGenerate; i++) {
			List<Point> river = new ArrayList<>();

			// generate the river points
			// start in the mountains
			// https://stackoverflow.com/questions/2043783/how-to-efficiently-performance-remove-many-items-from-list-in-java
			// Linked list structure is better for removing items since it just has to change node connections
			List<Point> mtnPositions = new LinkedList<>(Arrays.asList(continentData.mountains()));

			Point riverPos = Maths.tttr(mtnPositions, rand)
					.lerp(0.5, Maths.tttr(mtnPositions, rand));

			river.add(riverPos);

			// follow the river path until it hits its lowest point or sea level
			while (true) {
				double x = riverPos.getX();
				double y = riverPos.getY();

				// current river height
				double h = this.sampleContinentBase(continentData, (int) x, (int) y);

				// leave if in the ocean.
				// might have to stretch a bit further in the future just in case but she'll be right bro
				// intellij really thinks it's the CEO of english grammar huh
				if (h < 0) {
					break;
				}

				double riverSearchStep = this.riverStep;
				int searchSteps = 0;
				// height positive-y, etc
				double hPx = 0, hPy = 0, hNy = 0, hNx = 0;

				do {
					searchSteps++;

					// calculate surrounding heights
					hPy = this.sampleContinentBase(continentData, (int) x, (int) (y + riverSearchStep));
					hPx = this.sampleContinentBase(continentData, (int) (x + riverSearchStep), (int) y);
					hNy = this.sampleContinentBase(continentData, (int) x, (int) (y - riverSearchStep));
					hNx = this.sampleContinentBase(continentData, (int) (x - riverSearchStep), (int) y);

					// if stuck in a ditch, flood-fill search for the exit, then make a mad dash
					riverSearchStep += this.riverStep;
					//if (searchSteps > 1) System.out.printf("%d >> %.3f | %.3f %.3f %.3f %.3f\n", searchSteps, h, hPy, hPx, hNy, hNx);
				} while (hPx >= h && hPy >= h && hNx >= h && hNy >= h);

				// calculate vector directions for flow based on height difference
				// negative - positive to get downwards flow
				double dy = hNy - hPy;
				double dx = hNx - hPx;

				// normalise and multiply to size 'riverStep'
				double normalisationFactor = 1.0 / Math.sqrt(dy * dy + dx * dx);

				dy *= normalisationFactor * this.riverStep;
				dx *= normalisationFactor * this.riverStep;

				// next point(s)
				for (int j = 0; j < searchSteps; j++) {
					x += dx;
					y += dy;
					riverPos = new Point(x, y);
					river.add(riverPos);
				}
			}

			// add to our continent data rivers
			continentData.rivers().add(river.toArray(new Point[river.size()]));
		}

		return continentData;
	}

	// inputs in chunk landscape
	public int _testVoronoiPoints(int x, int y, boolean raw, boolean innerLines) {
		final int chunkScaleShift = 4;

		// continent diminished diameter
		double cDimDiameter = this.continentDiameter >> chunkScaleShift;

		// axes for every ~1000 blocks
		if (x % (5 * (200 >> chunkScaleShift)) == 0) return 0;
		if (y % (5 * (200 >> chunkScaleShift)) == 0) return 0;

		if (innerLines) {
			// axes for every ~200 blocks
			if (x % (200 >> chunkScaleShift) == 0) return Maths.rgb(100, 100, 100);
			if (y % (200 >> chunkScaleShift) == 0) return Maths.rgb(100, 100, 100);
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
		if (sqrDist < Maths.sqr(cDimRad + (100 >> chunkScaleShift))) {
			return Maths.rgb(0, 160, 160);
		}
		else {
			return Maths.rgb(0, 60, 120);
		}
	}

	/**
	 * Calculates the height at a position, incorporating continental features without blending, useful for testing.
	 *
	 * Inputs in block coordinate landscape
	 * Output in blocks between -128 and 256 and treat sea level = 0 ({@code h < 0 = sea, h >= 0 = land})
	 * Of course, in minecraft, sea level will be 63. However that is cringe so I shifted it down to 0.
	 * If implementing this in your work, make sure to adjust the exact generation to and input/output space to suit your game.
	 */
	public double _testContinentBase(ContinentData continentData, int x, int y) {
		// base heightmap is done via radial + noise
		for (Point p : continentData.mountains()) {
			if (p.squaredDist(x, y) < 25 * 25) return 256;
		}

		for (Point[] ps : continentData.rivers()) {
			for (Point p : ps) {
				if (p.squaredDist(x, y) < 10 * 10) return -128;
			}
		}

		// scale that dist of radius = 1, then invert and clamp
		double radial = 1.0 - continentData.centre().distance(x, y) / (continentDiameter * 0.5);

		// manipulate, sum, clamp
		return Math.max(-64, 0
				+ 60 * (radial - 0.2)
				+ 30 * this.noise.sample(x * BASE_DISTORT_FREQUENCY, y * BASE_DISTORT_FREQUENCY)
				+ 30 * radial * Math.max(0, this.noise.sample(x * BASE_HILLS_FREQUENCY, y * BASE_HILLS_FREQUENCY)) // hills
		);
	}

	/**
	 * Calculates the height at a position, incorporating river features without blending, useful for testing.
	 *
	 * Inputs in block coordinate landscape
	 * Output in blocks between -128 and 256 and treat sea level = 0 ({@code h < 0 = sea, h >= 0 = land})
	 * Of course, in minecraft, sea level will be 63. However that is cringe so I shifted it down to 0.
	 * If implementing this in your work, make sure to adjust the exact generation to and input/output space to suit your game.
	 */
	public double _testContinentRiver(ContinentData continentData, int x, int y) {
		// base heightmap is done via radial + noise
		for (Point[] ps : continentData.rivers()) {
			for (Point p : ps) {
				if (p.squaredDist(x, y) < 10 * 10) return -128;
			}
		}

		// base height
		return this.sampleContinentBase(continentData, x, y);
	}

	public static final double BASE_DISTORT_FREQUENCY = 1.0 / 850.0;
	public static final double BASE_HILLS_FREQUENCY = 1.0 / 300.0;
}
