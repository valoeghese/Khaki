package valoeghese.strom.utils;

// heavily cached because lloyd relaxation is very intensive lmao
// the way I do it at least
// i should probably write an optimised version of this that just creates double arrays rather than resampling each time
public class LloydVoronoi {
	// lloyd relaxation voronoi
	public LloydVoronoi(long seed, double relaxation) {
		this.seed = seed;
		this.relaxation = relaxation;

		// how many iterations of lloyd do you want?
		// yes
		this.crutch2 = new SimpleCache(512, (gridX, gridY) -> {
			double vxy[] = this.crutch.sample(gridX, gridY);

			if (gridX != 0 || gridY != 0) {
				double vx = vxy[0];
				double vy = vxy[1];

				// lloyd relaxation
				// im not following the algorithm strictly but imma call it that it's close enough

				// store originals
				final double originalVx = vx;
				final double originalVy = vy;

				// push points based on proximity to surrounding raw positions
				for (int xoo = -1; xoo <= 1; xoo++) { // consistency 100
					int gridXX = gridX + xoo;

					for (int yoo = -1; yoo <= 1; yoo++) {
						int gridYY = gridY + yoo;

						double neighbour[] = this.crutch.sample(gridXX, gridYY);
						vx -= this.relaxation * 0.11 * Math.max(0, 1.0 - neighbour[0] + originalVx);
						vy -= this.relaxation * 0.11 * Math.max(0, 1.0 - neighbour[1] + originalVy);
					}
				}

				vxy[0] = vx;
				vxy[1] = vy;
			}

			return vxy;
		});

		this.crutch3 = new SimpleCache(512, (gridX, gridY) -> {
			double vxy[] = this.crutch2.sample(gridX, gridY);

			if (gridX != 0 || gridY != 0) {
				double vx = vxy[0];
				double vy = vxy[1];

				// lloyd relaxation
				// im not following the algorithm strictly but imma call it that it's close enough

				// store originals
				final double originalVx = vx;
				final double originalVy = vy;

				// push points based on proximity to surrounding raw positions
				for (int xoo = -1; xoo <= 1; xoo++) { // consistency 100
					int gridXX = gridX + xoo;

					for (int yoo = -1; yoo <= 1; yoo++) {
						int gridYY = gridY + yoo;

						double neighbour[] = this.crutch2.sample(gridXX, gridYY);
						vx -= this.relaxation * 0.11 * Math.max(0, 1.0 - neighbour[0] + originalVx);
						vy -= this.relaxation * 0.11 * Math.max(0, 1.0 - neighbour[1] + originalVy);
					}
				}

				vxy[0] = vx;
				vxy[1] = vy;
			}

			return vxy;
		});

		this.crutch4 = new SimpleCache(512, (gridX, gridY) -> {
			double vxy[] = this.crutch3.sample(gridX, gridY);

			if (gridX != 0 || gridY != 0) {
				double vx = vxy[0];
				double vy = vxy[1];

				// lloyd relaxation
				// im not following the algorithm strictly but imma call it that it's close enough

				// store originals
				final double originalVx = vx;
				final double originalVy = vy;

				// push points based on proximity to surrounding raw positions
				for (int xoo = -1; xoo <= 1; xoo++) { // consistency 100
					int gridXX = gridX + xoo;

					for (int yoo = -1; yoo <= 1; yoo++) {
						int gridYY = gridY + yoo;

						double neighbour[] = this.crutch3.sample(gridXX, gridYY);
						vx -= this.relaxation * 0.11 * Math.max(0, 1.0 - neighbour[0] + originalVx);
						vy -= this.relaxation * 0.11 * Math.max(0, 1.0 - neighbour[1] + originalVy);
					}
				}

				vxy[0] = vx;
				vxy[1] = vy;
			}

			return vxy;
		});
	}

	private final long seed;
	private final double relaxation;

	private final SimpleCache<double[]> crutch = new SimpleCache(1024, (gridX, gridY) -> {
		double vxy[] = new double[2];

		if (gridX != 0 || gridY != 0) {
			vxy[0] = gridX + this.randomDouble(gridX, gridY, 0);
			vxy[1] = gridY + this.randomDouble(gridX, gridY, 1);
		}

		return vxy;
	});

	private final SimpleCache<double[]> crutch2;
	private final SimpleCache<double[]> crutch3;
	private final SimpleCache<double[]> crutch4;

	// centred lloyd
	public Point sampleC(double x, double y) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double rx = 0;
		double ry = 0;
		double rdist = 1000;

		for (int xo = -2; xo <= 2; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -2; yo <= 2; ++yo) {
				int gridY = baseY + yo;
				double vxy[] = this.crutch4.sample(gridX, gridY);
				double vx = vxy[0];
				double vy = vxy[1];

				double vdist = Voronoi.squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Point(rx, ry);
	}

	private int randomInt(int x, int y, long salt) {
		long v = this.seed;

		v *= 6364136223846793005L * v + 1442695040888963407L;
		v += x + salt;
		v *= 6364136223846793005L * v + 1442695040888963407L;
		v += y + salt;
		v *= 6364136223846793005L * v + 1442695040888963407L;
		v += x;
		v *= 6364136223846793005L * v + 1442695040888963407L;
		v += y;

		return (int) (v & 0xFFFFFFFFL); // 32 bits
	}

	private double randomDouble(int x, int y, long salt) {
		return Math.abs((double) this.randomInt(x, y, salt) * Voronoi.DOUBLE_UNIT);
	}
}
