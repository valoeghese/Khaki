package valoeghese.khaki.utils;

public final class Voronoi {
	public Voronoi(long seed, double relaxation) {
		this.seed = seed;
		this.relaxation = relaxation;
		this.unrelaxation = 1.0 - this.relaxation;
	}

	private final long seed;
	private final double relaxation;
	private final double unrelaxation;
	public boolean shaveCorners = false;

	public Point sampleGrid(int x, int y) {
		double vx = x + this.relaxation * 0.5 + this.unrelaxation * this.randomDouble(x, y, 0);
		double vy = y + this.relaxation * 0.5 + this.unrelaxation * this.randomDouble(x, y, 1);
		return new Point(vx, vy);
	}

	public Point sampleGridC(int x, int y) {
		if (x == 0 && y == 0) return Point.ORIGIN;
		double vx = x + this.unrelaxation * this.randomDouble(x, y, 0);
		double vy = y + this.unrelaxation * this.randomDouble(x, y, 1);
		return new Point(vx, vy);
	}

	public Point sampleGrid(int x) {
		double k = this.randomDouble(x, 42069, 0);
		double vx = x + this.relaxation * 0.5 + this.unrelaxation * k;
		return new Point(vx, 0);
	}

	public Point sample(double x, double y) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double rx = 0;
		double ry = 0;
		double rdist = 1000;

		for (int xo = -2; xo <= 2; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -2; yo <= 2; ++yo) {
				if (this.shaveCorners && xo * xo == 4 && yo * yo == 4) continue; // shave off corners

				int gridY = baseY + yo;

				double vx = gridX + this.relaxation * 0.5 + this.unrelaxation * this.randomDouble(gridX, gridY, 0);
				double vy = gridY + this.relaxation * 0.5 + this.unrelaxation * this.randomDouble(gridX, gridY, 1);

				double vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Point(rx, ry);
	}
	
	public Point sampleC(double x, double y) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double rx = 0;
		double ry = 0;
		double rdist = 1000;

		for (int xo = -2; xo <= 2; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -2; yo <= 2; ++yo) {
				if (this.shaveCorners && xo * xo == 4 && yo * yo == 4) continue; // shave off corners

				int gridY = baseY + yo;
				double vx = 0;
				double vy = 0;

				if (gridX != 0 || gridY != 0) {
					vx = gridX + this.unrelaxation * this.randomDouble(gridX, gridY, 0);
					vy = gridY + this.unrelaxation * this.randomDouble(gridX, gridY, 1);
				}

				double vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Point(rx, ry);
	}

	public Point sample(double x) {
		final int baseX = (int) Math.floor(x);
		double rx = 0;
		double rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			double vx = gridX + this.relaxation * 0.5 + this.unrelaxation * this.randomDouble(gridX, 42069, 0);
			double vdist = squaredDist(x, 0, vx, 0);

			if (vdist < rdist) {
				rx = vx;
				rdist = vdist;
			}
		}

		return new Point(rx, 0);
	}

	public MultiD1D2Result sampleD1D2Worley(double x, double y) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double rdist2 = 1000;
		double rdist = 1000;
		// closest point's grid X and Y
		int closestGridX = 0;
		int closestGridY = 0;

		for (int xo = -2; xo <= 2; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -2; yo <= 2; ++yo) {
				if (this.shaveCorners && xo * xo == 4 && yo * yo == 4) continue; // shave off corners

				int gridY = baseY + yo;

				double vx = gridX + this.relaxation * 0.5 + this.unrelaxation * this.randomDouble(gridX, gridY, 0);
				double vy = gridY + this.relaxation * 0.5 + this.unrelaxation * this.randomDouble(gridX, gridY, 1);
				double vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rdist2 = rdist;
					rdist = vdist;
					closestGridX = gridX;
					closestGridY = gridY;
				} else if (vdist < rdist2) {
					rdist2 = vdist;
				}
			}
		}

		return new MultiD1D2Result(Math.sqrt(rdist2) - Math.sqrt(rdist), closestGridX, closestGridY);
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
		return Math.abs((double) this.randomInt(x, y, salt) * DOUBLE_UNIT);
	}

	static final double DOUBLE_UNIT = 1.0 / (double) Integer.MAX_VALUE;

	static double squaredDist(double x0, double y0, double x1, double y1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		return dx * dx + dy * dy;
	}

	public record MultiD1D2Result(double value, int closestGridX, int closestGridY) {}
}