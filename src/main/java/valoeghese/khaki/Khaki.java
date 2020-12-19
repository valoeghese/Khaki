package valoeghese.khaki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import valoeghese.khaki.gen.KhakiBiomeSource;
import valoeghese.khaki.gen.KhakiChunkGenerator;

public class Khaki implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Khaki");

	@Override
	public void onInitialize() {
		Registry.register(Registry.BIOME_SOURCE, new Identifier("khaki", "khaki"), KhakiBiomeSource.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier("khaki", "khaki"), KhakiChunkGenerator.CODEC);
	}
}
