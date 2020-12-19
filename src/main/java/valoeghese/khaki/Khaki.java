package valoeghese.khaki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import valoeghese.khaki.gen.KhakiBiomeSource;
import valoeghese.khaki.gen.KhakiChunkGenerator;
import valoeghese.khaki.gen.KhakiNoiseGenerator;

public class Khaki implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Khaki");

	@Override
	public void onInitialize() {
		Registry.register(Registry.BIOME_SOURCE, new Identifier("khaki", "khaki"), KhakiBiomeSource.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier("khaki", "khaki"), KhakiChunkGenerator.CODEC);

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) { // Dev Test Command
			CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
				LiteralArgumentBuilder<ServerCommandSource> lab = CommandManager.literal("findRiver").executes(cmd -> {
					try {
						ServerPlayerEntity entity = cmd.getSource().getPlayer();
						ServerWorld world = entity.getServerWorld();
						ChunkGenerator generator = world.getChunkManager().getChunkGenerator();

						if (generator instanceof KhakiChunkGenerator) {
							KhakiNoiseGenerator terrain = ((KhakiChunkGenerator) generator).getNoiseGenerator();

							int schunkX = entity.chunkX;
							int schunkZ = entity.chunkZ;

							for (int cxo = -16; cxo < 16; ++cxo) {
								int chunkX = schunkX + cxo;

								for (int czo = -16; czo < 16; ++czo) {
									int chunkZ = schunkZ + czo;

									if (terrain.chunkSeesRiver(chunkX, chunkZ) > 0) {
										cmd.getSource().sendFeedback(new LiteralText((chunkX << 4) + ", " + (chunkZ << 4)), false);
										return 1;
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace(System.out);
					}

					return 0;
				});
				dispatcher.register(lab);
			});
		}
	}
}
