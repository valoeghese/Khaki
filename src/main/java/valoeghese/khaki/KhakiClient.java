package valoeghese.khaki;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.world.GeneratorType;
import valoeghese.khaki.gen.KhakiGeneratorType;

public class KhakiClient implements ClientModInitializer {
	public static GeneratorType generatorType;

	@Override
	public void onInitializeClient() {
		generatorType = new KhakiGeneratorType();
	}
}
