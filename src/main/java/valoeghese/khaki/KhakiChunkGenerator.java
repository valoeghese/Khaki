package valoeghese.khaki;

import java.util.Random;
import java.util.stream.IntStream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.noise.NoiseSampler;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;

public final class KhakiChunkGenerator extends ChunkGenerator {

	public KhakiChunkGenerator(BiomeSource biomeSource, long seed) {
		super(biomeSource, new StructuresConfig(true));
		this.seed = seed;
		this.random = new ChunkRandom(seed);
		this.surfaceDepthNoise = new OctaveSimplexNoiseSampler(this.random, IntStream.rangeClosed(-3, 0));
	}

	private final ChunkRandom random;
	private final long seed;
	private final NoiseSampler surfaceDepthNoise;

	public long getWorldSeed() {
		return this.seed;
	}

	@Override
	protected Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long seed) {
		return new KhakiChunkGenerator(this.biomeSource, seed);
	}

	// Identical to vanilla
	@Override
	public void buildSurface(ChunkRegion region, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		int chunkX = chunkPos.x;
		int chunkZ = chunkPos.z;
		ChunkRandom rand = new ChunkRandom();
		rand.setTerrainSeed(chunkX, chunkZ);

		final int startX = chunkPos.getStartX();
		final int startZ = chunkPos.getStartZ();
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for(int xo = 0; xo < 16; ++xo) {
			for(int zo = 0; zo < 16; ++zo) {
				int x = startX + xo;
				int z = startZ + zo;
				int height = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, xo, zo) + 1;
				double noise = this.surfaceDepthNoise.sample((double)x * 0.0625D, (double)z * 0.0625D, 0.0625D, (double)xo * 0.0625D) * 15.0D;
				region.getBiome(mutable.set(x, height, z)).buildSurface(rand, chunk, x, z, height, noise, Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), this.getSeaLevel(), region.getSeed());
			}
		}

		this.buildBedrock(chunk, rand);
	}

	private void buildBedrock(Chunk chunk, Random random) {
		ChunkPos chunkPos = chunk.getPos();
		final int startX = chunkPos.getStartX();
		final int startZ = chunkPos.getStartZ();
		BlockPos.Mutable pos = new BlockPos.Mutable();

		for(int xo = 0; xo < 16; ++xo) {
			pos.setX(xo + startX);

			for(int zo = 0; zo < 16; ++zo) {
				pos.setZ(zo + startZ);

				for (int y = 0; y < 4; ++y) {
					pos.setY(y);

					if (random.nextInt(5) >= y) {
						chunk.setBlockState(pos, Blocks.BEDROCK.getDefaultState(), false);
					}
				}
			}
		}
	}

	@Override
	public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getHeight(int x, int z, Type heightmapType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BlockView getColumnSample(int x, int z) {
		// TODO Auto-generated method stub
		return null;
	}

	public static final Codec<KhakiChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
		return instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((khakiChunkGenerator) -> {
			return khakiChunkGenerator.biomeSource;
		}), Codec.LONG.fieldOf("seed").stable().forGetter((khakiChunkGenerator) -> {
			return khakiChunkGenerator.getWorldSeed();
		})).apply(instance, instance.stable(KhakiChunkGenerator::new));
	});
}
