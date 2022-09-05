package valoeghese.strom;

import valoeghese.strom.utils.GridBox;
import valoeghese.strom.utils.Maths;
import valoeghese.strom.utils.Noise;
import valoeghese.strom.utils.Point;
import valoeghese.strom.utils.Voronoi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class TerrainGenerator {
	// settings. change these.
	public int continentDiameter;
	public int riverInterpolationSteps;
	public int mountainsPerRange;
	// merge threshold for river node points, i.e. the smallest manhattan distance at which they become the same point
	public double mergeThreshold;
	public double riverStep;

	// optional settings. you can change these
	public Consumer<String> warn = s -> {};

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
	 * Calculates the height at a position, excluding rivers and any local alterations ignored by rivers.
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
		// todo smoother transitions between mountains?
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
			weightedMtnHeight += mtnStrength * p.getHeight();
			weightsSum += mtnStrength;
		}

		// max mtn strength affects terrain
		height = Maths.lerp(maxMtnStrength, height, weightedMtnHeight / weightsSum);

		return height;
	}

	public ContinentData pregenerateContinentData(Point centre) {
		// random for pregen
		Random rand = new Random(centre.hashCode() + this.seed);

		Point[] mountainRange = this.generateMountainRange(rand, centre);

		return this.generateRivers(new ContinentData(centre, mountainRange, new GridBox<>(GRID_BOX_SIZE, 1 + this.continentDiameter / (2 * GRID_BOX_SIZE))), rand);
	}

	private final int minMtnHeight = 140;
	private final int deltaMtnHeight = 255 - minMtnHeight;
	// IF CHANGING MOUNTAIN HEIGHT ALGORITHM, REPLACE ALL INSTANCES OF rand.nextInt(deltaMtnHeight) + minMtnHeight

	private Point[] generateMountainRange(Random rand, Point centre) {
		final int nMtns = this.mountainsPerRange;

		Point[] mountainRange = new Point[nMtns];
		// use generateCenteredStartEnd if you want the range centered around the continent centre
		this.generateMountainStartEnd(rand, centre, mountainRange);

		Point start = mountainRange[0];
		Point end = mountainRange[nMtns - 1];

		for (int i = 1; i < nMtns - 1; i++) {
			mountainRange[i] = start.lerp((double) i / (double)(nMtns - 1), end).add(
					(rand.nextDouble() - 0.5) * 0.05 * this.continentDiameter,
					(rand.nextDouble() - 0.5) * 0.05 * this.continentDiameter,
					rand.nextInt(deltaMtnHeight) + minMtnHeight
			);
		}

		return mountainRange;
	}

	private void generateMountainStartEnd(Random rand, Point centre, Point[] mountainRange) {
		double chainX = (rand.nextDouble() - 0.5) * 0.33 * this.continentDiameter + centre.getX();
		double chainY = (rand.nextDouble() - 0.5) * 0.33 * this.continentDiameter + centre.getY();

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

		while (Math.abs(eDX) + Math.abs(eDY) < 0.225 * this.continentDiameter) {
			//System.out.println("Amplifying Point Spread");
			eDX *= 1.3;
			eDY *= 1.3;
		}

		Point end = new Point(
				chainX + eDX,
				chainY + eDY,
				rand.nextInt(deltaMtnHeight) + minMtnHeight
		);

		mountainRange[0] = start;
		mountainRange[mountainRange.length - 1] = end;
	}

	private void generateCenteredStartEnd(Random rand, Point centre, Point[] mountainRange) {
		// Use same dX and dY algorithm
		double eDX = (rand.nextDouble() - 0.5) * 0.5 * this.continentDiameter; // endDX
		double eDY =  (rand.nextDouble() - 0.5) * 0.5 * this.continentDiameter; // endDY
		//System.out.println((Math.abs(eDX) + Math.abs(eDY)) / this.continentDiameter);

		// inb4 edX and eDy ~= 0 and the game gets stuck in a very big loop
		// sike let's harcode a fix for that
		if (Math.abs(eDX) + Math.abs(eDY) < 0.002 * this.continentDiameter) {
			//System.out.println("Applying emergency EDX offset");
			eDX += (0.5 + rand.nextDouble()) * 0.3 * this.continentDiameter;
		}

		while (Math.abs(eDX) + Math.abs(eDY) < 0.225 * this.continentDiameter) {
			//System.out.println("Amplifying Point Spread");
			eDX *= 1.3;
			eDY *= 1.3;
		}

		// treat as diameter
		eDX /= 2;
		eDY /= 2;

		mountainRange[0] = new Point(
				centre.getX() - eDX,
				centre.getY() - eDX,
				rand.nextInt(deltaMtnHeight) + minMtnHeight
		);

		mountainRange[mountainRange.length - 1] = new Point(
				centre.getX() + eDX,
				centre.getY() + eDY,
				rand.nextInt(deltaMtnHeight) + minMtnHeight
		);
	}

	private ContinentData generateRivers(ContinentData continentData, Random rand) {
		final int nRiversToGenerate = 10;

		for (int i = 0; i < nRiversToGenerate; i++) {
			List<Point> river = new ArrayList<>();

			// generate the river points
			// start in the mountains
			// https://stackoverflow.com/questions/2043783/how-to-efficiently-performance-remove-many-items-from-list-in-java
			// Linked list structure is better for removing items since it just has to change node connections
			List<Point> mtnPositions = new LinkedList<>(Arrays.asList(continentData.mountains()));

			Point riverNodePos = Maths.tttr(mtnPositions, rand)
					.lerp(rand.nextDouble() * 0.2 + 0.5, Maths.tttr(mtnPositions, rand));

			double x = riverNodePos.getX();
			double y = riverNodePos.getY();
			// current river height
			double h = this.sampleContinentBase(continentData, (int) x, (int) y);
			river.add(riverNodePos.withValue((int) h));

			// the node to merge with, when two rivers combine.
			Node merge = null;
			double lastHeight = Double.MAX_VALUE;

			// for trying to get unstuck from oscillations between points when in a pit
			boolean forcedSearchStep = false;

			// follow the river path until it hits its lowest point or sea level
			while (true) {
				// x, y, and h should all be set to match the latest point in the river by here
				// they used to be set here, however due to requirements in the algorithm they are set elsewhere in this method

				// leave if in the ocean.
				// might have to stretch a bit further in the future just in case but she'll be right bro
				// intellij really thinks it's the CEO of english grammar huh
				if (h < 0) {
					break;
				}

				double riverSearchStep = this.riverStep;
				int searchSteps = 0;
				// height positive-y, etc
				double hPx, hPy, hNy, hNx;

				do {
					if (++searchSteps == 2) forcedSearchStep = false;

					// calculate surrounding heights
					hPy = this.sampleContinentBase(continentData, (int) x, (int) (y + riverSearchStep));
					hPx = this.sampleContinentBase(continentData, (int) (x + riverSearchStep), (int) y);
					hNy = this.sampleContinentBase(continentData, (int) x, (int) (y - riverSearchStep));
					hNx = this.sampleContinentBase(continentData, (int) (x - riverSearchStep), (int) y);

					// if stuck in a ditch, flood-fill search for the exit, then make a mad dash
					riverSearchStep += this.riverStep;
					//if (searchSteps > 1) System.out.printf("%d >> %.3f | %.3f %.3f %.3f %.3f\n", searchSteps, h, hPy, hPx, hNy, hNx);
				} while (forcedSearchStep || (hPx >= h && hPy >= h && hNx >= h && hNy >= h));

				// calculate vector directions for flow based on height difference
				// negative - positive to get downwards flow
				// retreating from high points seems to yield better results going down, vice versa for up
				// just gonna use the centre difference
				double dx = hNx - hPx;
				double dy = hNy - hPy;

				// normalise and multiply to size 'riverStep'
				double normalisationFactor = 1.0 / Math.sqrt(dy * dy + dx * dx);
				dy *= normalisationFactor * this.riverStep;
				dx *= normalisationFactor * this.riverStep;

				// next point(s)
				// cursed solution to get points to consistently be descending even if skipping over/through hills out of a ditch
				List<Point> nextPoints = new LinkedList<>();
				// keep track of this
				lastHeight = h;

				// Go through adding the X/Y/H positions of nodes. Use the terrain height for now. We will adjust heights later.
				riverPointAdder:
				for (int j = 0; j < searchSteps; j++) {
					x += dx;
					y += dy;
					riverNodePos = new Point(x, y);

					// keep track of any other rivers that need to be redirected
					List<Node> nodesToRedirect = new ArrayList<>();

					// check for nodes to merge with in this gridbox
					// cant be bothered using compute power to search neighbours so cope. if they get close enough across borders I'm sure they'll merge soon after
					// remaining nearby nodes to search

					// clone it into a linked list so can concurrently modify the data while iterating
					LinkedList<Node> nearbyNodes = new LinkedList<>(continentData.rivers().get((int) x, (int) y));

					while (nearbyNodes.size() > 0) {
						Node node = nearbyNodes.removeFirst();

						if (Math.abs(node.current().getX() - x) + Math.abs(node.current().getY() - y) <= this.mergeThreshold) {
							// It can flow to the node if the node position is lower or equal to the last height it flows from
							if ((int) node.current().getHeight() <= (int) lastHeight) {
								//System.out.println("merging...");
								merge = node;
								nextPoints.add(node.current()); // add the node position instead of the close position to the node

								// redirect
								for (Node redirectMeDaddy : nodesToRedirect) {
									// from the river to redirect,
									continentData.rivers().add(
											(int) node.current().getX(),
											(int) node.current().getY(),
											new Node(
													redirectMeDaddy.previous(),
													node.current()
											)
									);
								}
								break riverPointAdder;
							}
							// Else, get the node to flow to *it* (and destroy the original river)
							// Only if they're close enough though (20 blocks)
							else if ((int) node.current().getHeight() - 30 <= (int) lastHeight) {
								//System.out.println("redirecting...");

								// recursively delete children of the node
								Node deleteMeDaddy = node;
								do {
									continentData.rivers().remove((int) deleteMeDaddy.current().getX(), (int) deleteMeDaddy.current().getY(), deleteMeDaddy);
									// don't iterate over this in future either, in case we haven't got to you
									nearbyNodes.remove(deleteMeDaddy);
								} while ((deleteMeDaddy = deleteMeDaddy.next) != null);

								// reimplement node
								// don't implement it just yet in case it redirects somewhere else tho
								// e.g. this pt cld be between a higher and lower river which by themselves are not close at all
								nodesToRedirect.add(node);
							}
						}
					}

					// Note that redirections are all to the current node.

					x = riverNodePos.getX();
					y = riverNodePos.getY();
					// current river height
					h = this.sampleContinentBase(continentData, (int) x, (int) y);
					nextPoints.add(riverNodePos.withValue((int) h));

					// redirect
					for (Node redirectMeDaddy : nodesToRedirect) {
						// from the river to redirect,
						continentData.rivers().add(
								(int) x,
								(int) y,
								new Node(
										redirectMeDaddy.previous(),
										riverNodePos
								)
						);
					}
				}

				// base this on what was *actually* found.
				forcedSearchStep = lastHeight < h;

				// ok, now we should check the points are going at least flat, hopefully downstream.
				// h is the final height of the points to add

				// case 1, the less common case. If it's trying to go uphill, go flat instead.
				// also do this if it's actually going flat. it's easier
				if (h >= lastHeight) {
					for (Point p : nextPoints) {
						river.add(p.withValue((int) lastHeight));
					}

					// update the height of the final one.
					h = lastHeight;
				}
				// case 2: it's flowing downstream. make sure the points are *actually* going downstream.
				// additionally, ensure that it doesn't create a U shape by ensuring it doesn't go below the final point.
				else {
					int prevAddedHeight = (int) lastHeight;

					for (Point p : nextPoints) {
						int nextAddedHeight = (int) p.getHeight();

						// if any points go higher than the previous one, make it the same height as previous one
						if (nextAddedHeight > prevAddedHeight) nextAddedHeight = prevAddedHeight;
						// if any points go lower than the last one, make it the same height as the last one
						if (nextAddedHeight < (int) h) nextAddedHeight = (int) h;

						river.add(p.withValue(nextAddedHeight));

						prevAddedHeight = nextAddedHeight;
					}
				}

				// if we merged, leave.
				if (merge != null) {
					break;
				}

				// also no.
				if (river.size() > 10_000) {
					this.warn.accept("Took longer than 10 thousand iterations to find a river path... forcing end!");
					break;
				}
			}

			// todo, what if a point is filtered out that another node depends on?
			//river = this.smoothRiverPoints(river);

			// convert to nodes
			// and add to our continent data rivers
			// todo river width values stored
			Point previous = Point.NONE;
			Node previousNode = DUMMY_NODE; // micro-op. no if statements ;)

			// Smooth River Points while adding
			// First create a frame so we can see which points are added from this river.
			GridBox<Node> frame = continentData.rivers().createFrame();

			for (Point point : river) {
				try {
					Node node = new Node(previous, point);
					node = this.smoothRiverNodes(frame, node);

					previousNode.next = node; // store in case this river needs to be redirected
					frame.add((int) point.getX(), (int) point.getY(), node);

					previousNode = node;
				} catch (ArrayIndexOutOfBoundsException e) {
					this.warn.accept("River went out of bounds of continent grid box! Previous and Offending positions follow:");
					this.warn.accept(previous.getX() + ", " + previous.getY());
					this.warn.accept(point.getX() + ", " + point.getY());
					return continentData;
				}

				previous = point;
			}
		}

		return continentData;
	}

	public Node smoothRiverNodes(GridBox<Node> frame, Node newNode) {
		// test for points that should be smoothed to iron out formations like zigzags and twirls while it searches for a way out
		// this is probably a faster way of doing it than smoothRiverPoints

		// ================== WARNING ========================
		// >>> This method assumes the node before the one about to be added has NULL as the next node <<<
		// If that behaviour changes, the code for this method must be updated

		final double riverSmoothConstant = 1;
		final double riverSmoothRad = riverSmoothConstant * this.riverStep;

		Point newPoint = newNode.current();
		int gridX = frame.gridSpace((int) newPoint.getX());
		int gridY = frame.gridSpace((int) newPoint.getY());

		// look in neighbouring cells
		for (int xo = -1; xo <= 1; xo++) {
			for (int yo = -1; yo <= 1; yo++) {
				// make a copy so as not to have a concurrent modification exception
				Iterable<Node> nodes = new ArrayList<>(frame.getGridBox(gridX + xo, gridY + yo));

				for (Node n : nodes) {
					// ignore points that have since been removed
					if (!frame.containsInGridBox(gridX + xo, gridY + yo, n)) continue;

					Point existingPoint = n.current();

					// don't bother smoothing if it's just the previous node in the series
					// otherwise, if it's close enough to redirect, just do it
					if (n.next != null && newPoint.squaredDist(existingPoint) <= riverSmoothRad * riverSmoothRad) {
						// remove points up until the new node (or a required node for a merge)
						Node removeMe = n;

						while ((removeMe = removeMe.next) != null) {
							frame.remove((int) removeMe.current().getX(), (int) removeMe.current().getY(), removeMe);
						};

						// substitute the next node to be added with a "summary" node, going from prev of the first removed node to current
						// that way the river still flows consistently
						newNode = new Node(n.current(), newPoint);
					}
				}
			}
		}

		return newNode;
	}

	// OLD WAY OF DOING IT
	// DONT USE IT
	// HERE FOR COMPARISON AND REFERENCE PURPOSES
	public List<Point> smoothRiverPoints(List<Point> points) {
		// test for points that should be smoothed to iron out formations like zigzags and twirls while it searches for a way out
		// this is the slowest way to do it. don't be slow. don't check the entire river path.
		// this itself is like 1/3 of the time spent pregenerating
		// This could probably be made a crap ton easier through doing it via node redirection instead
		final double riverSmoothConstant = 1;

		final double riverSmoothRad = riverSmoothConstant * this.riverStep;

		// max size is points.size(), so preallocate that
		List<Point> seen = new ArrayList<>(points.size());

		// go through, copying points in...
		for (Point pNew : points) {

			// look through all but the previous point to be added when checking if they're too close
			for (int i = 0; i < seen.size() - 1; i++) {
				Point pSeen = seen.get(i);

				// if new is close to a seen point to where it should have been connected, redirect the river to connect to it
				// this is our "smooth" operation
				if (pNew.squaredDist(pSeen) <= riverSmoothRad * riverSmoothRad) {
					// cut off anything after pSeen and redirect the flow
					//System.out.println("Smoothing...");
					seen = seen.subList(0, i + 1); // + 1 because upper bound is exclusive. include the seen point that new should connect to.
					break;
				}
			}

			seen.add(pNew);
		}

		return seen;
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
			int value = point.hashCode();
			boolean origin = (value == Point.ORIGIN.hashCode() && (int) voronoiX == 0 && (int) voronoiY == 0);
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
		// mountain info
		for (Point p : continentData.mountains()) {
			if (p.squaredDist(x, y) < 25 * 25) return 256;
		}

		// base heightmap is done via radial + noise
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
		// river info
		int gridX = continentData.rivers().gridSpace(x);
		int gridY = continentData.rivers().gridSpace(y);

		for (int gxo = -1; gxo <= 1; gxo++) {
			for (int gyo = -1; gyo <= 1; gyo++) {
				for (Node node : continentData.rivers().getGridBox(gridX + gxo, gridY + gyo)) {
					if (node.current().squaredDist(x, y) < 8 * 8) return -128;
				}
			}
		}

		// base height
		return this.sampleContinentBase(continentData, x, y);
	}

	private static final Node DUMMY_NODE = new Node(Point.NONE, Point.NONE);
	public static final double BASE_DISTORT_FREQUENCY = 1.0 / 850.0;
	public static final double BASE_HILLS_FREQUENCY = 1.0 / 300.0;
	public static final int GRID_BOX_SIZE = 64;
}
