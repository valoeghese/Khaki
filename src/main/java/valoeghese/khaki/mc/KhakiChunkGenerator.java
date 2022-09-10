package valoeghese.khaki.mc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
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
	public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
		// Loop x/z and do it like the old days
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor level) {
		// TODO
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

		// TODO

		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
		// TODO (dont bother accounting for rivers in ocean floor)
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
		// TODO
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
	}

	// pretty important stuff

	@Override
	public int getMinY() {
		return -64;
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
