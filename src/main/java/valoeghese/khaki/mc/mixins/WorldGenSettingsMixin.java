package valoeghese.khaki.mc.mixins;

import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valoeghese.khaki.mc.KhakiChunkGenerator;

import java.util.Optional;

@Mixin(WorldGenSettings.class)
public class WorldGenSettingsMixin {
	@Shadow
	@Final
	@Mutable
	private Registry<LevelStem> dimensions;

	@Inject(at = @At("RETURN"), method = "<init>(JZZLnet/minecraft/core/Registry;Ljava/util/Optional;)V")
	private void onCreation(long seed, boolean generateStructures, boolean generateBonusChest, Registry<LevelStem> levelStems, Optional<String> legacyCustomOptions, CallbackInfo ci) {
		LevelStem prevOverworld = this.dimensions.get(LevelStem.OVERWORLD);

		// The vanilla chunk generator is DEAD! Worldgen now is MINE >:)
		// warning: don't actually do this in production. this is a prototype so it's alg
		if (prevOverworld.generator() instanceof NoiseBasedChunkGenerator nbcc) {
			ChunkGeneratorAccessor cga = (ChunkGeneratorAccessor) (Object) nbcc;
			this.dimensions = WorldGenSettings.withOverworld(this.dimensions, prevOverworld.typeHolder(), new KhakiChunkGenerator(cga.getStructureSets(), cga.getBiomeSource(), seed));
		}
	}
}
