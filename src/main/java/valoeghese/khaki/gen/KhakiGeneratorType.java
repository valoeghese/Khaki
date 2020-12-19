package valoeghese.khaki.gen;

import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public class KhakiGeneratorType extends GeneratorType {
	public KhakiGeneratorType() {
		super("khaki");
	}

	@Override
	protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> settingsRegistry, long seed) {
		return new KhakiChunkGenerator(new KhakiBiomeSource(seed, biomeRegistry), seed);
	}
}
