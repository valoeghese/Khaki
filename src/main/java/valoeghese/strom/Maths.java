package valoeghese.strom;

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
}