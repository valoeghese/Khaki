package valoeghese.khaki;

import java.util.Random;

import net.minecraft.util.math.MathHelper;
import valoeghese.khaki.noise.OpenSimplexNoise;
import valoeghese.khaki.util.DoubleGridOperator;
import valoeghese.khaki.util.LossyDoubleCache;

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
	}

	private final long seed;

	private DoubleGridOperator continentNoise;

	private static final double redistribute(double f) {
		double c = (f - 70.0) / 120.0;
		return 70.0 + 210.0 * (c / (1.0 + Math.abs(c)));
	}

	public int getBaseHeight(int megaChunkX, int megaChunkZ) {
		int result = (int) this.continentNoise.get(megaChunkX, megaChunkZ);
		if (result < 20) {
			return 20;
		}
		if (result > 140) {
			return 140;
		}
		return result;
	}
}

