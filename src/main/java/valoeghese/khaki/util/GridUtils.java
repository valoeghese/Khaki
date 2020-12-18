package valoeghese.khaki.util;

public class GridUtils {
	public static double distanceLineBetween(double startX, double startZ, double endX, double endZ, int x, int z) {
		/*if (startX > endX || startZ > endZ) {
			double tempX = startX;
			startX = endX;
			endX = tempX;
			
			double tempZ = startZ;
			startZ = endZ;
			endZ = tempZ;
		}*/

		double dx = endX - startX;
		double dz = endZ - startZ;

		// try fix bugs by swappings all x and z and doing it backwards
		if (Math.abs(dz) > Math.abs(dx)) {
			// cache old vals
			double oldDX = dx;
			double oldSX = startX;
			double oldEX = endX;
			int oldX = x;

			// swap
			dx = dz;
			startX = startZ;
			endX = endZ;
			x = z;
			
			dz = oldDX;
			startZ = oldSX;
			endZ = oldEX;
			z = oldX;
		}

		double m = dz / dx;
		double targetZ = m * x + startZ - m * startX;
		return Math.abs(z - targetZ);
	}
}
