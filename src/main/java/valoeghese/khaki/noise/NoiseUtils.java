package valoeghese.khaki.noise;

import net.minecraft.util.math.MathHelper;

public final class NoiseUtils {
	public static Vec2f sampleVoronoiGrid(int x, int y, int seed) {
		float vx = x + (randomFloat(x, y, seed) + 0.5f) * 0.5f;
		float vy = y + (randomFloat(x, y, seed + 1) + 0.5f) * 0.5f;
		return new Vec2f(vx, vy);
	}

	public static Vec2f sampleEvenVoronoi(float x, float y, int seed) {
		final int baseX = MathHelper.floor(x);
		final int baseY = MathHelper.floor(y);
		float rx = 0;
		float ry = 0;
		float rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				// ensure more evenly distributed
				float vx = gridX + (randomFloat(gridX, gridY, seed) + 0.5f) * 0.5f;
				float vy = gridY + (randomFloat(gridX, gridY, seed + 1) + 0.5f) * 0.5f;
				float vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Vec2f(rx, ry);
	}

	public static Vec2f sampleVoronoi(float x, float y, int seed) {
		final int baseX = MathHelper.floor(x);
		final int baseY = MathHelper.floor(y);
		float rx = 0;
		float ry = 0;
		float rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				// ensure more evenly distributed
				float vx = gridX + randomFloat(gridX, gridY, seed);
				float vy = gridY + randomFloat(gridX, gridY, seed + 1);
				float vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Vec2f(rx, ry);
	}

	public static Vec2f sampleManhattanVoronoi(float x, float y, int seed) {
		final int baseX = MathHelper.floor(x);
		final int baseY = MathHelper.floor(y);
		float rx = 0;
		float ry = 0;
		float rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				// ensure more evenly distributed
				float vx = gridX + randomFloat(gridX, gridY, seed);
				float vy = gridY + randomFloat(gridX, gridY, seed + 1);
				float vdist = manhattanDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Vec2f(rx, ry);
	}

	public static int random(int x, int y, int seed, int mask) {
		// seed *= seed * 6364136223846793005L + 1442695040888963407L are constants in biome layers
		// this seems to work fine. I have had with some variatns of this times where seems to be always one value but hooopefully this is fine
		seed *= 375462423 * seed + 672456235;
		seed += x;
		seed *= 375462423 * seed + 672456235;
		seed += y;

		return seed & mask;
	}

	private static float squaredDist(float x0, float y0, float x1, float y1) {
		float dx = Math.abs(x1 - x0);
		float dy = Math.abs(y1 - y0);
		return dx * dx + dy * dy;
	}

	private static float manhattanDist(float x0, float y0, float x1, float y1) {
		float dx = Math.abs(x1 - x0);
		float dy = Math.abs(y1 - y0);
		return dx + dy;
	}

	public static float randomFloat(int x, int y, int seed) {
		return (float) random(x, y, seed, 0xFFFF) / (float) 0xFFFF;
	}
}
