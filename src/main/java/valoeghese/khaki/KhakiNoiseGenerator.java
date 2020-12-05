package valoeghese.khaki;

import java.util.Random;

import net.minecraft.util.math.MathHelper;
import valoeghese.khaki.noise.NoiseUtils;
import valoeghese.khaki.noise.OpenSimplexNoise;
import valoeghese.khaki.util.DoubleGridOperator;
import valoeghese.khaki.util.IntGridOperator;
import valoeghese.khaki.util.LossyDoubleCache;
import valoeghese.khaki.util.LossyIntCache;

/**
 * Separated from the chunk generator for outside-of-minecraft testing purposes.
 */
public class KhakiNoiseGenerator {
	public KhakiNoiseGenerator(long seed) {
		this.seed = seed;
		Random rand = new Random();

		OpenSimplexNoise continent = new OpenSimplexNoise(rand);
		OpenSimplexNoise continent2 = new OpenSimplexNoise(rand);

		this.continentNoise = new LossyDoubleCache(1024, (x, z) -> 65 + 20 * (MathHelper.sin(x * 0.2f) + MathHelper.sin(z * 0.2f)) + 70 * continent.sample(x * 0.125, z * 0.125) + 20 * continent2.sample(x * 0.5, z * 0.5));
		this.positionData = new LossyIntCache(1024, (x, z) ->  {
			int result = 0;

			if (this.getBaseHeight(x, z) <= SEA_LEVEL) {
				if (this.getBaseHeight(x + 1, z) > SEA_LEVEL
						|| this.getBaseHeight(x - 1, z) > SEA_LEVEL
						|| this.getBaseHeight(x, z + 1) > SEA_LEVEL
						|| this.getBaseHeight(x, z - 1) > SEA_LEVEL)  {
					result |= 1; // coast

					if (NoiseUtils.random(x, z, (int) this.seed, 0b111) == 0) {
						result |= 2; // river start
					}
				}

				result |= NoiseUtils.random(x, z, 1 + (int) this.seed, 0b111111111100); // 0b 1111 1111 1100. 6 more bits of random data
			}

			return result;
		});
	}

	private final long seed;

	private final DoubleGridOperator continentNoise;
	private final IntGridOperator positionData;

	private static final double redistribute(double f) {
		double c = (f - 70.0) / 120.0;
		return 70.0 + 210.0 * (c / (1.0 + Math.abs(c)));
	}

	public int getBaseHeight(int megaChunkX, int megaChunkZ) {
		int result = (int) this.continentNoise.get(megaChunkX, megaChunkZ);
		// clamp base height from 20 to 150
		if (result < 20) {
			return 20;
		}
		if (result > 150) {
			return 150;
		}
		return result;
	}

	public static final int SEA_LEVEL = 80;
}

