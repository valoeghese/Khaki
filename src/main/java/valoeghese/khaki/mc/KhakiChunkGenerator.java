package valoeghese.khaki.mc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class KhakiChunkGenerator extends ChunkGenerator {
	public static final Codec<KhakiChunkGenerator> CODEC =
			RecordCodecBuilder.create(instance -> commonCodec(instance)
					.and(BiomeSource.CODEC.fieldOf("biomesource").stable().forGetter(cg -> cg.biomeSource))
					.and(Codec.LONG.fieldOf("seed").orElseGet(() -> 1L).stable().forGetter(chunkGenerator -> chunkGenerator.seed))
					.apply(instance, KhakiChunkGenerator::new)
	);

	private final long seed;
	private final KhakiMC khaki;

	public KhakiChunkGenerator(Registry<StructureSet> rss, BiomeSource source, long seed) {
		super(rss, Optional.empty(), source);
		this.seed = seed;
		this.khaki = new KhakiMC(seed);
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
		Heightmap oceanFloorHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap surfaceHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

		ChunkPos chunkPos = chunk.getPos();

		this.khaki.fillChunk(chunk, chunkPos, surfaceHeightmap, oceanFloorHeightmap);
		this.khaki.addRiverDecoration(chunk, chunkPos, surfaceHeightmap, oceanFloorHeightmap);

		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
		ChunkPos pos = chunk.getPos();

		// Loop x/z and do it like the old days
		for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++)
		{
			for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
				this.khaki.genTerrainBlocks(chunk, x, z);
			}
		}
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor level) {
		return getSeaLevel();
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
		int terrainHeight = this.khaki.getTerrainHeight(x, z);

		if (type.isOpaque().test(KhakiMC.WATER)) {
			return Math.max(terrainHeight, getSeaLevel());
		}
		else {
			return terrainHeight;
		}
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
		BlockState[] result = new BlockState[level.getHeight()];
		int terrainHeight = this.khaki.getTerrainHeight(x, z);

		for (int i = 0; i < level.getHeight(); i++) {
			int y = i + level.getMinBuildHeight();
			result[i] = y < terrainHeight ? KhakiMC.STONE : (y < getSeaLevel() ? KhakiMC.WATER : KhakiMC.AIR );
		}

		return new NoiseColumn(level.getMinBuildHeight(), result);
	}

	// Not necessary for prototype

	@Override
	public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
	}

	@Override
	public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion level) {
		// from NoiseBasedChunkGenerator
		ChunkPos chunkPos = level.getCenter();
		Holder<Biome> biome = level.getBiome(chunkPos.getWorldPosition().atY(level.getMaxBuildHeight() - 1));

		WorldgenRandom rand = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
		rand.setDecorationSeed(level.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());

		NaturalSpawner.spawnMobsForChunkGeneration(level, biome, chunkPos, rand);
	}

	// pretty important stuff

	@Override
	public int getMinY() {
		return this.khaki.getMinY();
	}

	@Override
	public int getGenDepth() {
		return 384;
	}

	@Override
	public int getSeaLevel() {
		return this.khaki.getSeaLevel();
	}
}
