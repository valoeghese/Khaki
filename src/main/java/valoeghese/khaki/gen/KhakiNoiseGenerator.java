package valoeghese.khaki.gen;

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

		this.continent = new OpenSimplexNoise(rand);
		this.continent2 = new OpenSimplexNoise(rand);

		this.continentNoise = new LossyIntCache(512, (x, z) -> this.sampleHeight(x << 8, z << 8));

		this.positionData = new LossyIntCache(512, (x, z) ->  {
			int result = 0;

			if (this.getBaseMegaHeight(x, z) < SEA_LEVEL) {
				if (this.getBaseMegaHeight(x + 1, z) >= SEA_LEVEL
						|| this.getBaseMegaHeight(x - 1, z) >= SEA_LEVEL
						|| this.getBaseMegaHeight(x, z + 1) >= SEA_LEVEL
						|| this.getBaseMegaHeight(x, z - 1) >= SEA_LEVEL)  {
					result |= 1; // coast

					//if (NoiseUtils.random(x, z, this.iseed, 0b111) == 0) {
					if ((x & 1) == 0 && (z & 1) == 0) {
						// if ((x & 0b11) == 0 && (z & 0b11) == 0) { // place on a grid. TODO use spaced random
						result |= 2; // river start
					}
				}
			}

			result |= NoiseUtils.random(x, z, 1 + this.iseed, 0b111111111100);  // 0b 1111 1111 1100. 6 more bits of random data

			return result;
		});

		this.rivers = new LossyIntCache(256, (x, z) -> {
			if (this.getBaseMegaHeight(x, z) >= SEA_LEVEL) {
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

				// pythag(8,8) + 16 = 11.32(ceil-2dp) + 16 = 27.32. TODO generate this based on a constant.
				if (GridUtils.distanceLineBetween(startX, startZ, endX, endZ, (x << 4) + 8, (z << 4) + 8) < 27.32) {
					result++; // result is number of rivers to check for in this chunk.
				}

				riverData >>= 4;
			}

			return result;
		});

		this.baseHeight = new LossyIntCache(1024, (x, z) -> {
			return this.sampleHeight(x, z);
		});

		OpenSimplexNoise hills = new OpenSimplexNoise(rand);

		this.heightModifier = new LossyDoubleCache(256, (x, z) -> {
			return 10 * (1 + hills.sample(x * 0.02, z * 0.02));
		});

		this.heightmap = new LossyIntCache(256, (x, z) -> {
			final int chunkX = (x >> 4);
			final int chunkZ = (z >> 4);
			final int megaChunkX = (x >> 8);
			final int megaChunkZ = (z >> 8);

			// TODO: make river height have more interesting modifications like waterfalls
			int baseHeight = this.getBaseBlockHeight(x, z);
			double modifier = this.heightModifier.get(x, z);

			double riverDist = 16.00;
			int riverData = this.getRiverData(megaChunkX, megaChunkZ);

			if (riverData > 0 && this.chunkSeesRiver(chunkX, chunkZ) > 0) {
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

					double dist = GridUtils.distanceLineBetween(startX, startZ, endX, endZ, x, z);

					if (dist < riverDist) {
						riverDist = dist;
					}

					riverData >>= 4;
				}
			}

			// River shape yeah baby
			if (riverDist < 16.00) {
				if (riverDist < 1.00) {
					modifier = -4;
				} else {
					modifier = MathHelper.lerp((riverDist - 1.00) / 15.00, -4, modifier);
				}
			}

			return baseHeight + (int) modifier;
		});
	}

	private final OpenSimplexNoise continent;
	private final OpenSimplexNoise continent2;

	private final long seed;
	private final int iseed;
	private static final double SAMPLE_HEIGHT_CONSTANT = (1.0 / 256.0);

	private final IntGridOperator continentNoise;
	private final IntGridOperator positionData;
	private final IntGridOperator rivers;
	private final DoubleGridOperator offsets;

	/**
	 * Gives the number of rivers a chunk has to check for in generation.
	 */
	private final IntGridOperator chunkRivers;
	private final IntGridOperator baseHeight;
	private final DoubleGridOperator heightModifier;
	private final IntGridOperator heightmap;

	private int sampleHeight(int x, int z) {
		return (int) (65 
				+ 20 * (MathHelper.cos((float) (x * SAMPLE_HEIGHT_CONSTANT * 0.2))
						+ MathHelper.sin((float) (z * SAMPLE_HEIGHT_CONSTANT * 0.2)))
				+ 70 * this.continent.sample(x * SAMPLE_HEIGHT_CONSTANT * 0.125, z * SAMPLE_HEIGHT_CONSTANT * 0.125)
				+ 20 * this.continent2.sample(x * SAMPLE_HEIGHT_CONSTANT * 0.5, z * SAMPLE_HEIGHT_CONSTANT * 0.5));
	}

	private int getRiverFrom(int x, int z, int checkX, int checkZ) {
		int rX = x;
		int rZ = z;
		int currentHeight = this.getBaseMegaHeight(rX, rZ);
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

				int newHeight = this.getBaseMegaHeight(nextX, nextZ);

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
			currentHeight = this.getBaseMegaHeight(rX, rZ);
			cache = direction.reverse();
		}

		return -1;
	}

	public int getBaseMegaHeight(int megaChunkX, int megaChunkZ) {
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

	public int getBaseBlockHeight(int x, int z) {
		return this.baseHeight.get(x, z);
	}

	public int getHeight(int x, int z) {
		return this.heightmap.get(x, z);
	}

	public int getPositionData(int megaChunkX, int megaChunkZ) {
		return this.positionData.get(megaChunkX, megaChunkZ);
	}

	public int getRiverData(int megaChunkX, int megaChunkZ) {
		return this.rivers.get(megaChunkX, megaChunkZ);
	}

	/**
	 * Used by the generator to optimise generation and by the visualiser to visualise river placement.
	 */
	public int chunkSeesRiver(int chunkX, int chunkZ) {
		return this.chunkRivers.get(chunkX, chunkZ);
	}

	public int getWaterHeight(int x, int z) {
		return Math.max(SEA_LEVEL, this.getBaseBlockHeight(x, z));
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

	public static final int SEA_LEVEL = 81; // was 80 but checks were <= and > rather than < and >=.
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