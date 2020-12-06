package valoeghese.khaki;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.math.Direction;
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

		// Multiplication by 0 bad
		int piseed = (int) (seed >> 32);
		this.iseed = piseed == 0 ? 1 : piseed;

		Random rand = new Random();

		OpenSimplexNoise continent = new OpenSimplexNoise(rand);
		OpenSimplexNoise continent2 = new OpenSimplexNoise(rand);

		this.continentNoise = new LossyIntCache(512, (x, z) -> (int) (65 + 20 * (MathHelper.cos(x * 0.2f) + MathHelper.sin(z * 0.2f)) + 70 * continent.sample(x * 0.125, z * 0.125) + 20 * continent2.sample(x * 0.5, z * 0.5)));
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
								IntList optionsXPreferred = new IntArrayList();
								IntList optionsZPreferred = new IntArrayList();
								List<GridDirection> directionsPreferred = new ArrayList<>();

								IntList optionsX = new IntArrayList();
								IntList optionsZ = new IntArrayList();
								List<GridDirection> directions = new ArrayList<>();

								// go through directions

								for (GridDirection direction : GridDirection.values()) {
									int nextX = rX + direction.xOff;
									int nextZ = rZ + direction.zOff;

									int newHeight = this.getBaseHeight(nextX, nextZ);

									if (newHeight > currentHeight + 10) {
										optionsXPreferred.add(nextX);
										optionsZPreferred.add(nextZ);
										directionsPreferred.add(direction);
									} else if (newHeight > currentHeight + 2) {
										optionsX.add(nextX);
										optionsZ.add(nextZ);
										directions.add(direction);
									}
								}

								// prefer the preferred
								if (!optionsXPreferred.isEmpty()) {
									optionsX = optionsXPreferred;
									optionsZ = optionsZPreferred;
									directions = directionsPreferred;
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

		this.offsets = new LossyDoubleCache(512, (x, z) -> (double) NoiseUtils.randomFloat(x, z, this.iseed));

		this.chunkRivers = new LossyIntCache(1024, (x, z) -> {
			int megaChunkX = (x >> 4);
			int megaChunkZ = (z >> 4);
			int riverData = this.getRiverData(megaChunkX, megaChunkZ);
			int result = 0;
			GridDirection[] currentRiverData = new GridDirection[2];

			while (riverData > 0) {
				GridDirection.deserialise(currentRiverData, riverData & 0b1111);

				// todo: not this
				// Actual method to use should be this
				// 1. get position along edge 1
				// 2. get position along edge 2
				double offset1a = this.offsets.get(megaChunkX + currentRiverData[0].xOff, megaChunkZ + currentRiverData[0].zOff);
				double offset1b = this.offsets.get(megaChunkX + currentRiverData[0].xOff + 32, megaChunkZ + currentRiverData[0].zOff);
				double startX = ((megaChunkX + currentRiverData[0].xOff) << 8) + 256 * offset1a;
				double startZ = ((megaChunkZ + currentRiverData[0].zOff) << 8) + 256 * offset1b;

				double offset2a = this.offsets.get(megaChunkX + currentRiverData[1].xOff, megaChunkZ + currentRiverData[1].zOff);
				double offset2b = this.offsets.get(megaChunkX + currentRiverData[1].xOff + 32, megaChunkZ + currentRiverData[1].zOff);
				double endX = ((megaChunkX + currentRiverData[1].xOff) << 8) + 256 * offset2a;
				double endZ = ((megaChunkZ + currentRiverData[1].zOff) << 8) + 256 * offset2b;

				riverData >>= 4;
			}

			return result;
		});
	}

	private final long seed;
	private final int iseed;

	private final IntGridOperator continentNoise;
	private final IntGridOperator positionData;
	private final IntGridOperator rivers;
	private final DoubleGridOperator offsets;
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

	static void deserialise(GridDirection[] directions, int number) {
		int iShape = number & 0b11;
		number >>= 2;
		number &= 0b11;

		directions[0] = BY_ID[number];

		GridShape shape = GridShape.BY_ID[iShape];
		
		switch (shape) {
		case ANTICLOCKWISE:
			directions[1] = BY_ID[(number - 1) & 0b11];
			break;
		case CLOCKWISE:
			directions[1] = BY_ID[(number + 1) & 0b11];
			break;
		case LINE:
			directions[1] = directions[0].reverse();
			break;
		case NODE:
			directions[1] = directions[0];
			break;
		default:
			throw new NullPointerException("Impossible error. Notify me (valoeghese) Immediately!. Debug Data: IShape = " + iShape + ", Direction = " + number + ", Array Size = " + directions.length);
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
			// TODO just make a bit expression. Probably "(to.id + 1) & 0b11 == from.id"
			result |= (to.id > from.id || (from.id == 3 && to.id == 0)) ? GridShape.CLOCKWISE.id : GridShape.ANTICLOCKWISE.id;
		}

		return result;
	}

	static final GridDirection[] BY_ID = new GridDirection[4];

	static {
		for (GridDirection d : GridDirection.values()) {
			BY_ID[d.id] = d;
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

	static final GridShape[] BY_ID = new GridShape[4];

	static {
		for (GridShape d : GridShape.values()) {
			BY_ID[d.id] = d;
		}
	}
}