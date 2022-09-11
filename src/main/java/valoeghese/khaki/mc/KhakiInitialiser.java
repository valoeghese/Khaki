package valoeghese.khaki.mc;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class KhakiInitialiser implements ModInitializer {
	@Override
	public void onInitialize() {
	}

	public static void onBootstrap(Registry<Codec<? extends ChunkGenerator>> registry) {
		Registry.register(registry, "khaki:khakimc", KhakiChunkGenerator.CODEC);
	}
}
