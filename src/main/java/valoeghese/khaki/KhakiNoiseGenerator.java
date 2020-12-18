package valoeghese.khaki;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.math.MathHelper;
import valoeghese.khaki.noise.NoiseUtils;
import valoeghese.khaki.noise.OpenSimplexNoise;
import valoeghese.khaki.util.DoubleGridOperator;
import valoeghese.khaki.util.GridDirection;
import valoeghese.khaki.util.GridUtils;
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

					//if (NoiseUtils.random(x, z, this.iseed, 0b111) == 0) {
					//					if ((x & 1) == 0 && (z & 1) == 0) {
					if ((x & 0b11) == 0 && (z & 0b11) == 0) { // place on a grid. TODO use spaced random
						result |= 2; // river start
					}
				}
			}

			result |= NoiseUtils.random(x, z, 1 + this.iseed, 0b111111111100);  // 0b 1111 1111 1100. 6 more bits of random data

			return result;
		});

		this.rivers = new LossyIntCache(256, (x, z) -> {
			if (this.getBaseHeight(x, z) > SEA_LEVEL) {
				int result = 0;

				for (int xo = -RIVER_SEARCH_RAD; xo <= RIVER_SEARCH_RAD; ++xo) {
					int xPos = x + xo;

					for (int zo = -RIVER_SEARCH_RAD; zo <= RIVER_SEARCH_RAD; ++zo) {
						int zPos = z + zo;

						// if river start
						if ((this.getPositionData(xPos, zPos) & 2) == 2) {
							int riverData = this.getRiverFrom(xPos, zPos, x, z);

							if (riverData > -1) {
								result <<= 4; // shift existing data: 2 positions (0b0000-0b1111)
								result |= riverData;
							}
						}
					}
				}

				return result;
			} else if ((this.getPositionData(x, z) & 2) == 2) {
				return this.getRiverFrom(x, z, x, z); // TODO simplify check since start is river?
			} else {
				return 0;
			}
		});

		this.offsets = new LossyDoubleCache(512, (x, z) -> (double) 192 * NoiseUtils.randomFloat(x, z, this.iseed) + 32.0);

		this.chunkRivers = new LossyIntCache(1024, (x, z) -> {
			int megaChunkX = (x >> 4);
			int megaChunkZ = (z >> 4);
			int riverData = this.getRiverData(megaChunkX, megaChunkZ);
			int result = 0;
			double[] edgeData = new double[2];
			GridDirection[] currentRiverData = new GridDirection[2];

			while (riverData > 0) {
				GridDirection.deserialise(currentRiverData, riverData & 0b1111);

				// 1. get position along edge 1
				edgePos(edgeData, currentRiverData[0], megaChunkX, megaChunkZ);
				double startX = edgeData[0];
				double startZ = edgeData[1];

				// 2. get position along edge 2
				double endX;
				double endZ;

				if (currentRiverData[0] == currentRiverData[1]) {
					// centre of region
					endX = (megaChunkX << 8) + 128;
					endZ = (megaChunkZ << 8) + 128;
				} else {
					edgePos(edgeData, currentRiverData[1], megaChunkX, megaChunkZ);
					endX = edgeData[0];
					endZ = edgeData[1];
				}

				// pythag(8,8) + 16 = 11.32(ceil-2dp) + 16 = 27.32.
				if (GridUtils.distanceLineBetween(startX, startZ, endX, endZ, (x << 4) + 8, (z << 4) + 8) < 27.32) {
					result++; // result is number of rivers to check for in this chunk.
				}

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
	/**
	 * Gives the number of rivers a chunk has to check for in generation.
	 */
	private final IntGridOperator chunkRivers;

	private int getRiverFrom(int x, int z, int checkX, int checkZ) {
		int rX = x;
		int rZ = z;
		int currentHeight = this.getBaseHeight(rX, rZ);
		GridDirection cache = null;
		Random riverRand = new Random(rX * 5724773 + rZ);

		// trace river path
		for (int i = 0; i < RIVER_SEARCH_RAD; ++i) {
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

				if (newHeight > currentHeight + 12) {
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
				if (rX == checkX && rZ == checkZ) {
					if (cache == null) {
						break;
					}

					return GridDirection.serialise(cache, null);
				}

				break; // no new positions
			}

			int index = riverRand.nextInt(optionsX.size());
			GridDirection direction = directions.get(index);

			if (rX == checkX && rZ == checkZ) {
				if (direction == null && cache == null) {
					break;
				}

				return GridDirection.serialise(cache, direction);
			}

			rX = optionsX.getInt(index);
			rZ = optionsZ.getInt(index);
			currentHeight = this.getBaseHeight(rX, rZ);
			cache = direction.reverse();
		}

		return -1;
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

	public int checkForRivers(int chunkX, int chunkZ) {
		return this.chunkRivers.get(chunkX, chunkZ);
	}

	private static final double redistribute(double f) {
		double c = (f - 70.0) / 120.0;
		return 70.0 + 210.0 * (c / (1.0 + Math.abs(c)));
	}

	private void edgePos(double position[], GridDirection direction, int megaChunkX, int megaChunkZ) {
		// Note: DOWN and LEFT are owned by the square. Since origin is bottom left corner.
		if (direction.horizontal) {
			double offset;

			if (direction == GridDirection.RIGHT) {
				position[0] = ((megaChunkX + 1) << 8);
				offset = this.offsets.get(megaChunkX + 1, megaChunkZ); // next square's left
			} else { // left
				position[0] = (megaChunkX << 8);
				offset = this.offsets.get(megaChunkX, megaChunkZ); // this square's left
			}

			position[1] = (megaChunkZ << 8) + offset;
		} else {
			// horizontal edge offsets (in vertical edges from centre) always has a +32 offset to x in order to be different
			double offset;

			if (direction == GridDirection.UP) {
				position[1] = ((megaChunkZ + 1) << 8);
				offset = this.offsets.get(megaChunkX + 32, megaChunkZ + 1); // next square's down
			} else { // left
				position[1] = (megaChunkZ << 8);
				offset = this.offsets.get(megaChunkX + 32, megaChunkZ); // this square's down
			}

			position[0] = (megaChunkX << 8) + offset;
		}
	}

	public static final int SEA_LEVEL = 80;
	public static final int RIVER_SEARCH_RAD = 8;

	public static enum GridShape {
		NODE(0),
		LINE(1),
		CLOCKWISE(2),
		ANTICLOCKWISE(3);

		private GridShape(int id) {
			this.id = id;
		}

		public final int id;

		public static final GridShape[] BY_ID = new GridShape[4];

		static {
			for (GridShape d : GridShape.values()) {
				BY_ID[d.id] = d;
			}
		}
	}	
}