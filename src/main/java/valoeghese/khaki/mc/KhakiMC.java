package valoeghese.khaki.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import valoeghese.khaki.TerrainGenerator;
import valoeghese.khaki.utils.Maths;

public class KhakiMC {
	public KhakiMC(long seed) {
		System.out.println("Creating Khaki Terrain Generator with seed " + seed);
		this.seed = seed;
		this.terrain = new TerrainGenerator(seed);
		this.logger = LoggerFactory.getLogger("KhakiMC @" + this.seed);

		// Initialise Settings
		this.terrain.continentDiameter = 4000;
		this.terrain.riverInterpolationSteps = 10;
		this.terrain.mountainsPerRange = 11;
		this.terrain.riverStep = 16;
		this.terrain.riverCount = 10;
		this.terrain.mergeThreshold = 12.0;
		this.terrain.warn = this.logger::warn;
	}

	private final Logger logger;
	private final long seed;
	private final TerrainGenerator terrain;
	private double[] fillChunkTerrainInfo = new double[3];
	private double[] buildSurfaceTerrainInfo = new double[3];

	public int getTerrainHeight(int x, int z) {
		double[] terrainInfo = new double[3];
		return this.getTerrainHeight(x, z, terrainInfo);
	}

	private int getTerrainHeight(int x, int z, double[] terrainInfo) {
		this.terrain.sampleHeight(x, z, terrainInfo);
		return 1 + getSeaLevel() + Maths.floor(terrainInfo[0]);
	}

	private int getRiverHeight(int x, int z, double[] terrainInfo) {
		this.terrain.sampleHeight(x, z, terrainInfo);
		return 1 + getSeaLevel() + Maths.floor(terrainInfo[1]);
	}

	public int getMinY() {
		return -64;
	}

	public int getSeaLevel() {
		return 63;
	}

	// Chunk filling. In a CC compatible way
	public void fillChunk(ChunkAccess chunk, ChunkPos chunkPos, Heightmap surfaceHeightmap, Heightmap oceanFloorHeightmap) {
		final int seaLevel = getSeaLevel();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
			pos.setX(x);

			for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
				pos.setZ(z);

				final int terrainHeight = this.getTerrainHeight(x, z, this.fillChunkTerrainInfo);
				final int surfaceHeight = Math.max(terrainHeight, seaLevel);
				final int maxHeight = Maths.clamp(chunk.getMinBuildHeight(), chunk.getMaxBuildHeight() - 1, surfaceHeight);

				BlockState toSet;

				for (int y = chunk.getMinBuildHeight(); y < maxHeight; y++) {
					pos.setY(y);

					if (y < terrainHeight) {
						toSet = STONE;
					}
					else if (y < seaLevel) {
						toSet = WATER;
					}
					else {
						// in case
						toSet = AIR;
					}

					chunk.setBlockState(pos, toSet, false);
					surfaceHeightmap.update(x & 0xF, y, z & 0xF, toSet);
					oceanFloorHeightmap.update(x & 0xF, y, z & 0xF, toSet);
				}
			}
		}
	}

	public void addRiverDecoration(ChunkAccess chunk, ChunkPos chunkPos, Heightmap surfaceHeightmap, Heightmap oceanFloorHeightmap) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
			pos.setX(x);

			for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
				pos.setZ(z);

				final int riverHeight = this.getRiverHeight(x, z, this.fillChunkTerrainInfo);
				final double riverDist = this.fillChunkTerrainInfo[2];
				final int riverWidth = (int) Maths.clampMap(riverHeight, 150,200, 4, 2);
				final int cutWidth = riverWidth + 4;

				if (riverDist <= cutWidth) {
					final int yVariation = (int) (0.75 * Math.sqrt(riverWidth * riverWidth - riverDist * riverDist));
					int y;

					for (y = riverHeight + yVariation; y >= riverHeight - yVariation - 1; y--) {
						if (y < chunk.getMinBuildHeight() || y >= chunk.getMaxBuildHeight()) continue;

						pos.setY(y);
						BlockState toSet = y < riverHeight - 1 ? WATER : AIR;

						chunk.setBlockState(pos, toSet, false);
						surfaceHeightmap.update(x & 0xF, y, z & 0xF, toSet);
						oceanFloorHeightmap.update(x & 0xF, y, z & 0xF, toSet);
					}

					// always place gravel below rivers & other cut bits
					if (y >= chunk.getMinBuildHeight() && y < chunk.getMaxBuildHeight()) {
						pos.setY(y);
						BlockState state = chunk.getBlockState(pos);

						// gravel floor and sides
						if (state == STONE && y >= getSeaLevel()) {
							chunk.setBlockState(pos, GRAVEL, false);
							surfaceHeightmap.update(x & 0xF, y, z & 0xF, GRAVEL);
							oceanFloorHeightmap.update(x & 0xF, y, z & 0xF, GRAVEL);
						}
					}
				}
			}
		}
	}

	public void genTerrainBlocks(ChunkAccess chunk, int x, int z) {
		int topBlockY = this.getTerrainHeight(x, z, this.buildSurfaceTerrainInfo) - 1;
		final double riverDist = this.buildSurfaceTerrainInfo[2];
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);

		for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
			pos.setY(y);
			BlockState currentState = chunk.getBlockState(pos);

			if (currentState == STONE) {
				if (y == topBlockY) {
					chunk.setBlockState(pos, topBlockY <= getSeaLevel() ? SAND : (riverDist < 12 ? GRAVEL : GRASS_BLOCK), false);
				}
				else if (y >= topBlockY - 3) {
					chunk.setBlockState(pos, topBlockY <= getSeaLevel() ? SANDSTONE : DIRT, false);
				}
			}
		}
	}

	private static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
	private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
	private static final BlockState SAND = Blocks.SAND.defaultBlockState();
	private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	private static final BlockState SANDSTONE = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
	static final BlockState STONE = Blocks.STONE.defaultBlockState();
	static final BlockState WATER = Blocks.WATER.defaultBlockState();
	static final BlockState AIR = Blocks.AIR.defaultBlockState();
}
