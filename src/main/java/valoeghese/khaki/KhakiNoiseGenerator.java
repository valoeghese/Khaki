package valoeghese.khaki;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.math.MathHelper;
import valoeghese.khaki.noise.NoiseUtils;
import valoeghese.khaki.noise.OpenSimplexNoise;
import valoeghese.khaki.util.IntGridOperator;
import valoeghese.khaki.util.LossyIntCache;

/**
 * Separated from the chunk generator for outside-of-minecraft testing purposes.
 */
public class KhakiNoiseGenerator {
	public KhakiNoiseGenerator(long seed) {
		this.seed = seed;

		// Multiplication by 0 bad
		int piseed = (int) (seed >> 32);
		this.iseed = piseed == 0 ? 1 : piseed;

		Random rand = new Random();

		OpenSimplexNoise continent = new OpenSimplexNoise(rand);
		OpenSimplexNoise continent2 = new OpenSimplexNoise(rand);

		this.continentNoise = new LossyIntCache(512, (x, z) -> (int) (65 + 20 * (MathHelper.sin(x * 0.2f) + MathHelper.sin(z * 0.2f)) + 70 * continent.sample(x * 0.125, z * 0.125) + 20 * continent2.sample(x * 0.5, z * 0.5)));
		this.positionData = new LossyIntCache(512, (x, z) ->  {
			int result = 0;

			if (this.getBaseHeight(x, z) <= SEA_LEVEL) {
				if (this.getBaseHeight(x + 1, z) > SEA_LEVEL
						|| this.getBaseHeight(x - 1, z) > SEA_LEVEL
						|| this.getBaseHeight(x, z + 1) > SEA_LEVEL
						|| this.getBaseHeight(x, z - 1) > SEA_LEVEL)  {
					result |= 1; // coast

					if (NoiseUtils.random(x, z, this.iseed, 0b111) == 0) {
						result |= 2; // river start
					}
				}

				result |= NoiseUtils.random(x, z, 1 + this.iseed, 0b111111111100); // 0b 1111 1111 1100. 6 more bits of random data
			}

			return result;
		});

		this.rivers = new LossyIntCache(256, (x, z) -> {
			if (this.getBaseHeight(x, z) > SEA_LEVEL) {
				int result = 0;

				for (int xo = -6; xo <= 6; ++xo) {
					int xPos = x + xo;

					for (int zo = -6; zo <= 6; ++zo) {
						int zPos = z + zo;

						// if river start
						if ((this.getPositionData(xPos, zPos) & 2) == 2) {
							result <<= 4; // shift existing data: 2 positions (0b0000-0b1111)

							int rX = xPos;
							int rZ = zPos;
							int currentHeight = this.getBaseHeight(rX, rZ);
							GridDirection cache = null;
							Random riverRand = new Random(rX * 5724773 + rZ);

							// trace river path
							for (int i = 0; i < 6; ++i) {
								IntList optionsX = new IntArrayList();
								IntList optionsZ = new IntArrayList();
								List<GridDirection> directions = new ArrayList<>();

								// go through directions

								for (GridDirection direction : GridDirection.values()) {
									int nextX = rX + direction.xOff;
									int nextZ = rZ + direction.zOff;

									int newHeight = this.getBaseHeight(nextX, nextZ);

									if (newHeight > currentHeight + 2) {
										optionsX.add(nextX);
										optionsZ.add(nextZ);
										directions.add(direction);
									}
								}

								if (optionsX.isEmpty()) {
									if (rX == x && rZ == z) {
										if (cache == null) {
											break;
										}

										result |= GridDirection.serialise(cache, null);
									}

									break; // no new positions
								}

								int index = riverRand.nextInt(optionsX.size());
								GridDirection direction = directions.get(index);

								if (rX == x && rZ == z) {
									if (direction == null && cache == null) {
										break;
									}

									result |= GridDirection.serialise(cache, direction);
									break;
								}

								rX = optionsX.getInt(index);
								rZ = optionsZ.getInt(index);
								cache = direction;
							}
						}
					}
				}

				return result;
			} else { // TODO river end traceback to start?
				return 0;
			}
		});
		
		this.chunkRivers = new LossyIntCache(1024);
	}

	private final long seed;
	private final int iseed;

	private final IntGridOperator continentNoise;
	private final IntGridOperator positionData;
	private final IntGridOperator rivers;
	private final IntGridOperator chunkRivers;

	private static final double redistribute(double f) {
		double c = (f - 70.0) / 120.0;
		return 70.0 + 210.0 * (c / (1.0 + Math.abs(c)));
	}

	public int getBaseHeight(int megaChunkX, int megaChunkZ) {
		int result = this.continentNoise.get(megaChunkX, megaChunkZ);
		// clamp base height from 20 to 150
		if (result < 20) {
			return 20;
		}
		if (result > 150) {
			return 150;
		}
		return result;
	}

	public int getPositionData(int megaChunkX, int megaChunkZ) {
		return this.positionData.get(megaChunkX, megaChunkZ);
	}

	public int getRiverData(int megaChunkX, int megaChunkZ) {
		return this.rivers.get(megaChunkX, megaChunkZ);
	}

	public static final int SEA_LEVEL = 80;
}

enum GridDirection {
	UP(0, 0, 1),
	RIGHT(1, 1, 0),
	DOWN(2, 0, -1),
	LEFT(3, -1, 0);

	private GridDirection(int id, int xOff, int zOff) {
		this.id = id;
		this.xOff = xOff;
		this.zOff = zOff;
	}

	final int id;
	final int xOff, zOff;

	GridDirection reverse() {
		switch (this) {
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		default:
			return null;
		}
	}

	static int serialise(GridDirection from, GridDirection to) {
		// both cannot be null.
		int result = 0;

		if (from == null) {
			result |= to.id;
			result <<= 2;
			result |= GridShape.NODE.id;
		} else if (to == null) {
			result |= from.id;
			result <<= 2;
			result |= GridShape.NODE.id;
		} else if (from == to.reverse()) {
			result |= from.id;
			result <<= 2;
			result |= GridShape.LINE.id;
		} else {
			result |= from.id;
			result <<= 2;
			result |= (to.id > from.id || (from.id == 3 && to.id == 0)) ? GridShape.CLOCKWISE.id : GridShape.ANTICLOCKWISE.id;
		}

		return result;
	}

	static final Int2ObjectMap<GridDirection> BY_ID = new Int2ObjectArrayMap<>();

	static {
		for (GridDirection d : GridDirection.values()) {
			BY_ID.put(d.id, d);
		}
	}
}

enum GridShape {
	NODE(0),
	LINE(1),
	CLOCKWISE(2),
	ANTICLOCKWISE(3);

	private GridShape(int id) {
		this.id = id;
	}

	final int id;
}