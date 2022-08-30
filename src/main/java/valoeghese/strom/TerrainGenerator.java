package valoeghese.strom;

import valoeghese.strom.utils.Voronoi;

public class TerrainGenerator {
	// settings. change these.
	public int continentRadius;
	public int riverInterpolationSteps;

	public TerrainGenerator(long seed) {
		this.seed = seed;

		// initialise generators
		this.voronoi = new Voronoi(seed, 0.2);
	}

	// internal stuff
	private final long seed;
	private final Voronoi voronoi;
}
