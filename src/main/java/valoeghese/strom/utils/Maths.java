package valoeghese.strom.utils;

public final class Maths {
	private Maths() {}

	public static double map(double value, double min, double max, double newmin, double newmax) {
		double prog = (value - min) / (max - min);
		return newmin + prog * (newmax - newmin);
	}

	public static double clampMap(double value, double min, double max, double newmin, double newmax) {
		double prog = (value - min) / (max - min);
		if (prog <= 0) return newmin;
		if (prog >= 1) return newmax;
		return newmin + prog * (newmax - newmin);
	}

	public static int floor(double d) {
		int i = (int) d;
		return d < i ? i - 1 : i;
	}

	public static double sqr(double d) {
		return d * d;
	}

	public static int rgb(int r, int g, int b) {
		return (((r << 8) | g) << 8) | b;
	}

	public static int grey(double g) {
		int i = (int) (g * 255);
		return rgb(i, i, i);
	}

	/**
	 * equivalent shift multiplier.
	 * The equivalent multiplier for a left shift (and divider for right shift).
	 * Useful for "shifting" doubles like ints
 	 */
	public static int eqShMul(int n) {
		return 2 << (n - 1);
	}
}