package valoeghese.khaki.mc.mixins;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGenerators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valoeghese.khaki.mc.KhakiInitialiser;

@Mixin(ChunkGenerators.class)
public class MixinChunkGenerators {
	@Inject(at = @At("RETURN"), method = "bootstrap")
	private static void onBootstrap(Registry<Codec<? extends ChunkGenerator>> registry, CallbackInfoReturnable<Codec<? extends ChunkGenerator>> cir) {
		KhakiInitialiser.onBootstrap(registry);
	}
}
