package valoeghese.khaki.gen;

import java.util.Random;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import valoeghese.khaki.noise.OpenSimplexNoise;

public class KhakiBiomeSource extends BiomeSource {
	public KhakiBiomeSource(long seed, Registry<Biome> biomeRegistry) {
		super(VanillaLayeredBiomeSource.BIOMES.stream()
				.map(biomeRegistry::get)
				.collect(Collectors.toList()));
		this.biomeRegistry = biomeRegistry;
		this.seed = seed;
	}

	private static final OpenSimplexNoise biomeNoise = new OpenSimplexNoise(new Random(0));
	private final Registry<Biome> biomeRegistry;
	private final long seed;

	@Override
	public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
		return biomeRegistry.get(biomeNoise.sample(biomeX * 0.06, biomeZ * 0.06) > 0.28 ? BiomeKeys.FOREST : BiomeKeys.PLAINS);
	}

	@Override
	public BiomeSource withSeed(long seed) {
		return new KhakiBiomeSource(seed, this.biomeRegistry);
	}

	@Override
	protected Codec<? extends BiomeSource> getCodec() {
		return CODEC;
	}

	public static final Codec<KhakiBiomeSource> CODEC = RecordCodecBuilder.create((instance) -> {
		return instance.group(Codec.LONG.fieldOf("seed")
				.stable()
				.forGetter(source -> source.seed),
				RegistryLookupCodec.of(Registry.BIOME_KEY)
				.forGetter(source -> source.biomeRegistry))
				.apply(instance, instance.stable(KhakiBiomeSource::new));
	});
}