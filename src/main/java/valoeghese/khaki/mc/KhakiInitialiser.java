package valoeghese.khaki.mc;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;

public class KhakiInitialiser implements ModInitializer {
	@Override
	public void onInitialize() {
		Registry.register(Registry.CHUNK_GENERATOR, "khaki:khakimc", KhakiChunkGenerator.CODEC);
	}
}
