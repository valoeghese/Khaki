package valoeghese.khaki.mc;

import valoeghese.khaki.TerrainGenerator;

public class KhakiMC {
	public KhakiMC(long seed) {
		this.seed = seed;
		this.terrain = new TerrainGenerator(seed);
	}

	private final long seed;
	private final TerrainGenerator terrain;
	private double[] terrainInfo = new double[3];

	public int getTerrainHeight(int x, int z) {
		this.terrain.sampleHeight(x, z, terrainInfo);
		return (int) terrainInfo[0];
	}

	public int getSeaLevel() {
		return 63;
	}

	// TODO chunk filling. in a CC compatible way

	// TODO build-surface on a column the old fashioned way

	// TODO river feature (carver). Probably make a nested non-static class
}
