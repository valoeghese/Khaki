package valoeghese.khaki.test.displays;

import valoeghese.khaki.utils.Maths;

public class LineBetweenDisplay implements Display {
	int n = 2;

	@Override
	public int getColour(int x, int y) {
		double d1 = 0.01 * Maths.distanceLineBetween(-200, -200, 200, 200, x, y);
		double d2 = 0.01 * Maths.distanceLineBetween(0, 0, 200, 0, x, y);
		double d3 = 0.01 * Maths.distanceLineBetween(0, 200, 0, 0, x, y);
		double d4 = 0.01 * Maths.distanceLineBetween(0, 200, 200, 200, x, y);

		double d = Maths.min(n + 1, d1, d2, d3, d4); // Math.min(0.005 * x, 0.005 * y)
		return Maths.grey(Maths.clamp(0, 1, d));
	}

	@Override
	public void modifyView(int direction) {
		n = (n + direction) & 3;
	}
}
