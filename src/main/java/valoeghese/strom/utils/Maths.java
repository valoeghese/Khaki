package valoeghese.strom.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

	public static double clamp(double min, double max, double value) {
		if (value <= min) return min;
		if (value >= max) return max;
		return value;
	}

	public static int floor(double d) {
		int i = (int) d;
		return d < i ? i - 1 : i;
	}

	public static double sqr(double d) {
		return d * d;
	}

	public static double cub(double d) {
		return d * d * d;
	}

	public static double pow4(double d) {
		return d * d * d * d;
	}

	public static double pow5(double d) {
		return d * d * d * d * d;
	}

	public static double pow6(double d) {
		return d * d * d * d * d
				* d;
	}

	public static double pow7(double d) {
		return d * d * d * d * d
				* d * d;
	}

	public static double pow8(double d) {
		return d * d * d * d * d
				* d * d * d;
	}

	public static int rgb(int r, int g, int b) {
		return (((r << 8) | g) << 8) | b;
	}

	public static int grey(double g) {
		int i = (int) (g * 255);
		return rgb(i, i, i);
	}

	/**
	 * Performs a linear interpolation between two values.
	 * @param progress a double between 0.0 and 1.0, indicating the progress from min to max.
	 *                 A value outside of this range will be extrapolated instead.
	 * @param min the minimum value of the interpolation.
	 * @param max the maximum value of the interpolation.
	 * @return the interpolated value.
	 */
	public static double lerp(double progress, double min, double max) {
		return min + progress * (max - min);
	}

	/**
	 * equivalent shift multiplier.
	 * The equivalent multiplier for a left shift (and divider for right shift).
	 * Useful for "shifting" doubles like ints
 	 */
	public static int eqShMul(int n) {
		return 2 << (n - 1);
	}

	// this operation is called ttt deal with it

	/**
	 * ttttt tttttt  ttttt
	 * @param ts t tttt ttttttttt
	 * @param tt tt ttttt ttt ttttttt ttttttt
	 * @param <T> t
	 * @return ttt t
	 */
	public static <T> T ttt(List<T> ts, Random tt) {
		// tt t (ts ttt)
		return ts.get(tt.nextInt(ts.size()));
	}

	/**
	 * ttttt tttttt REMOVE ttttt
	 * @param ts t ttr ttttttttt
	 * @param tt tt ttttt ttt ttttttt ttttttt
	 * @param <T> t
	 * @return ttt tr
	 */
	public static <T> T tttr(List<T> ts, Random tt) {
		return ts.remove(tt.nextInt(ts.size()));
	}

	public static boolean isPosApproximatelyAt(int x, int y, int tx, int ty, int leniency) {
		return x - leniency <= tx && x + leniency >= tx && y - leniency <= ty && y + leniency >= ty;
	}

	// Stolen from 2fc0f18. I guess we've come full circle
	// Stolen from Khaki
	public static double distanceLineBetween(double startX, double startY, double endX, double endY, int x, int y) {
		double dx = endX - startX;
		double dy = endY - startY;

		// try fix bugs by swappings all x and y and doing it backwards
		if (Math.abs(dy) > Math.abs(dx)) {
			// cache old vals
			double oldDX = dx;
			double oldSX = startX;
			double oldEX = endX;
			int oldX = x;

			// swap
			dx = dy;
			startX = startY;
			endX = endY;
			x = y;

			dy = oldDX;
			startY = oldSX;
			endY = oldEX;
			y = oldX;
		}

		double m = dy / dx;
		double targetY = m * x + startY - m * startX;

		// if beyond the ends, use euclidean dist to the ends
		if (targetY > endY) {
			dx = x - endX;
			dy = y - endY;
			return Math.sqrt(dx * dx + dy * dy);
		}
		else if (targetY < startY) {
			dx = x - startX;
			dy = y - startY;
			return Math.sqrt(dx * dx + dy * dy);
		}

		return Math.abs(y - targetY);
	}

	public static double min(double... doubles) {
		double min = Double.POSITIVE_INFINITY;

		for (double d: doubles) {
			if (d < min) min = d;
		}

		return min;
	}

	public static double min(int n, double... doubles) {
		n = Math.min(n, doubles.length);
		double min = Double.POSITIVE_INFINITY;

		for (int i = 0; i < n; i++) {
			double d = doubles[i];
			if (d < min) min = d;
		}

		return min;
	}
}