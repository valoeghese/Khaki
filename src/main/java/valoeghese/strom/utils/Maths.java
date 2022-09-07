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

	public static double invLerp(double min, double max, double value) {
		return (value - min) / (max - min);
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

	public static boolean isApproxEqu(double v1, double v2, double leniency) {
		return v1 - leniency <= v2 && v1 + leniency >= v2;
	}

	public static double fastInvSqrt(double x) { // https://stackoverflow.com/questions/11513344/how-to-implement-the-fast-inverse-square-root-in-java
		double xhalf = 0.5 * x;
		long i = Double.doubleToLongBits(x);
		i = 0x5fe6ec85e7de30daL - (i >> 1);
		x = Double.longBitsToDouble(i);
		x *= (1.5 - xhalf * x * x);
		x *= (1.5 - xhalf * x * x); // second iteration of newton's method
		return x;
	}

	public static double distanceLineBetween(double startX, double startY, double endX, double endY, double x, double y) {
		// observation 1: the distance to the line is the distance to the closest point. so we need to find the closest point
		// observation 2: the closest point between the bounds can be found by looking for the closest point out of bounds, then constraining it
		// observation 3: the line between a point and the closest point on an infinite line is perpendicular to said line
		// And I recall doing the calculation for perpendicular line gradient in maths class back when I was at high school! a/b -> -b/a
		// DsG

		// https://www.desmos.com/calculator/xd2arrblxx

		// =================================================
		//  Step 0: Order start/end by Y. So startY <= endY
		// =================================================
		if (startY > endY) {
			// switcheroo
			double tempEndY = endY;
			double tempEndX = endX;

			endY = startY;
			endX = startX;

			startY = tempEndY;
			startX = tempEndX;
		}

		// =====================================================
		//  Step 1: Get Gradients for Main & Perpendicular Line
		// =====================================================

		double dy = endY - startY;
		double dx = endX - startX;

		// normalise
		double invLen = fastInvSqrt(dy * dy + dx * dx);
		dy *= invLen;
		dx *= invLen;

		// =============================================
		//  (Step 1.5: Ensure no near-infinite numbers)
		// =============================================

		if (isApproxEqu(dy, 0, 0.002) || isApproxEqu(dx, 0, 0.002)) {
			// rotate by 45 degree matrix and perform the calculation again to increase accuracy
			// because division by tiny numbers ~= infinity
			// ah yes when sin(45 deg) != cos (45 deg)
			// approximation moment
			final double mat11 = 0.7071067811865476; //Math.cos(Math.PI / 4);
			final double mat12 = -0.7071067811865475; //-Math.sin(Math.PI / 4);
			final double mat21 = 0.7071067811865475; //Math.sin(Math.PI / 4);
			final double mat22 = 0.7071067811865476; //Math.cos(Math.PI / 4);

			double startXNew = mat11 * startX + mat12 * startY;
			double startYNew = mat21 * startX + mat22 * startY;

			double endXNew = mat11 * endX + mat12 * endY;
			double endYNew = mat21 * endX + mat22 * endY;

			double xNew = mat11 * x + mat12 * y;
			double yNew = mat21 * x + mat22 * y;

			return distanceLineBetween(startXNew, startYNew, endXNew, endYNew, xNew, yNew);
		}

		// main line gradient is m = dy/dx
		// perpendicular line gradient is m = -dx/dy

		// in addition to gradient, we have a point where each line must intersect
		// we can get +C from this via algebraic rearrangement:
		//   For known x, y and given m:
		//    m(x) + c = y
		//    c = y - m(x)
		// We will use (startX, startY) for the main line, and (x, y) for the perpendicular line
		// (endX, endY) also works for main line. the decision there was arbitrary

		// Notation: line-1 is the main line, line-2 is the perpendicular line.

		double m1 = dy / dx;
		double m2 = -dx / dy;

		// =========================================
		//  Step 2: Solve for Point Of Intersection
		// =========================================

		double c1 = startY - m1 * startX;
		double c2 = y - m2 * x;

		// Did some algebra (method of elimination on simultaneous equations) to find intersection point x and y in terms of m1, m2, c1, c2
		double iy = ((m1 / m2) * c2 - c1) / ((m1 / m2) - 1);
		// don't worry, m1 can never be = m2 because they are perpendicular
		double ix = (c2 - c1) / (m1 - m2);

		// ========================================================================
		//  Step 3: If iy and ix are out of line bounds, throw them back in bounds
		// ========================================================================

		// check by y since ensured to be ordered by y position
		if (iy < startY) {
			iy = startY;
			ix = startX;
		}
		else if (iy > endY) {
			iy = endY;
			ix = endX;
		}

		// =====================================================================
		//  Step 4: Finally, calculate euclidean distance to the point (ix, iy)
		// =====================================================================

		// repurpose dx and dy to mean axial distances between (x,y) and (ix,iy)
		dx = ix - x;
		dy = iy - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static Point closestPointLineBetween(Point start, Point end, double x, double y) {
		double startX	= start.getX(),	startY	= start.getY();
		double endX		= end.getX(),	endY	= end.getY();

		// see distanceLineBetween for a header comment giving more logic

		// https://www.desmos.com/calculator/xd2arrblxx

		// =================================================
		//  Step 0: Order start/end by Y. So startY <= endY
		// =================================================
		if (startY > endY) {
			// switcheroo
			double tempEndY = endY;
			double tempEndX = endX;

			endY = startY;
			endX = startX;

			startY = tempEndY;
			startX = tempEndX;
		}

		// =====================================================
		//  Step 1: Get Gradients for Main & Perpendicular Line
		// =====================================================

		double dy = endY - startY;
		double dx = endX - startX;

		// normalise
		double invLen = fastInvSqrt(dy * dy + dx * dx);
		dy *= invLen;
		dx *= invLen;

		// =============================================
		//  (Step 1.5: Ensure no near-infinite numbers)
		// =============================================

		if (isApproxEqu(dy, 0, 0.002) || isApproxEqu(dx, 0, 0.002)) {
			// rotate by 45 degree matrix and perform the calculation again to increase accuracy
			// because division by tiny numbers ~= infinity
			// ah yes when sin(45 deg) != cos (45 deg)
			// approximation moment
			final double mat11 = 0.7071067811865476; //  =  Math.cos(Math.PI / 4);
			final double mat12 = -0.7071067811865475; // = -Math.sin(Math.PI / 4);
			final double mat21 = 0.7071067811865475; //  =  Math.sin(Math.PI / 4);
			final double mat22 = 0.7071067811865476; //  =  Math.cos(Math.PI / 4);

			double startXNew = mat11 * startX + mat12 * startY;
			double startYNew = mat21 * startX + mat22 * startY;

			double endXNew = mat11 * endX + mat12 * endY;
			double endYNew = mat21 * endX + mat22 * endY;

			double xNew = mat11 * x + mat12 * y;
			double yNew = mat21 * x + mat22 * y;

			Point closestPointRotated = closestPointLineBetween(new Point(startXNew, startYNew, start.getHeight()), new Point(endXNew, endYNew, end.getHeight()), xNew, yNew);

			// apply inverse matrix to rotate back into normal space
			final double imat11 = 0.7071067811865476; //  =  Math.cos(-Math.PI / 4)	=  Math.cos(Math.PI / 4)
			final double imat12 = 0.7071067811865475; //  = -Math.sin(-Math.PI / 4) = +Math.sin(Math.PI / 4)
			final double imat21 = -0.7071067811865475; // =  Math.sin(-Math.PI / 4) = -Math.sin(Math.PI / 4)
			final double imat22 = 0.7071067811865476; //  =  Math.cos(-Math.PI / 4) =  Math.cos(Math.PI / 4)

			double ix = closestPointRotated.getX();
			double iy = closestPointRotated.getY();

			double ixNew = imat11 * ix + imat12 * iy;
			double iyNew = imat21 * ix + imat22 * iy;

			return new Point(ixNew, iyNew, closestPointRotated.getHeight());
		}

		// main line gradient is m = dy/dx
		// perpendicular line gradient is m = -dx/dy

		// in addition to gradient, we have a point where each line must intersect
		// we can get +C from this via algebraic rearrangement:
		//   For known x, y and given m:
		//    m(x) + c = y
		//    c = y - m(x)
		// We will use (startX, startY) for the main line, and (x, y) for the perpendicular line
		// (endX, endY) also works for main line. the decision there was arbitrary

		// Notation: line-1 is the main line, line-2 is the perpendicular line.

		double m1 = dy / dx;
		double m2 = -dx / dy;

		// =========================================
		//  Step 2: Solve for Point Of Intersection
		// =========================================

		double c1 = startY - m1 * startX;
		double c2 = y - m2 * x;

		// Did some algebra (method of elimination on simultaneous equations) to find intersection point x and y in terms of m1, m2, c1, c2
		double iy = ((m1 / m2) * c2 - c1) / ((m1 / m2) - 1);
		// don't worry, m1 can never be = m2 because they are perpendicular
		double ix = (c2 - c1) / (m1 - m2);

		// ========================================================================
		//  Step 3: If iy and ix are out of line bounds, throw them back in bounds
		// ========================================================================

		// check by y since ensured to be ordered by y position
		if (iy < startY) {
			iy = startY;
			ix = startX;
		}
		else if (iy > endY) {
			iy = endY;
			ix = endX;
		}

		return new Point(ix, iy, map(iy, startY, endY, start.getHeight(), end.getHeight()));
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