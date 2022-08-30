package valoeghese.strom.utils;

public class Point {
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
		this.value = this.hashCode() * 29 + Double.hashCode(this.x);
	}

	private Point(double x, double y, int value) {
		this.x = x;
		this.y = y;
		this.value = value;
	}

	private final double x;
	private final double y;
	private final int value;

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public int getValue() {
		return this.value;
	}

	public Point add(double x, double y) {
		return new Point(this.x + x, this.y + y);
	}

	public Point add(Point other) {
		return this.add(other.x, other.y);
	}

	public Point mul(double by) {
		return new Point(this.x * by, this.y * by, this.value);
	}

	public double squaredDist(Point other) {
		return this.squaredDist(other.x, other.y);
	}

	public double squaredDist(double x, double y) {
		double dx = Math.abs(x - this.x);
		double dy = Math.abs(y - this.y);
		return dx * dx + dy * dy;
	}

	public double distance(double x, double y) {
		return Math.sqrt(this.squaredDist(x, y));
	}

	public double distance(double x) {
		return Math.abs(x - this.x);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o == null) {
			return false;
		} else if (o instanceof Point) {
			Point vec2f = (Point) o;
			return vec2f.x == this.x && vec2f.y == this.y;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 7;
		result = 29 * result + Double.hashCode(this.x);
		result = 29 * result + Double.hashCode(this.y);
		return result;
	}

	@Override
	public String toString() {
		return "(" + this.x
				+ ", " + this.y
				+ ')';
	}

	/**
	 * Creates a point at the given position, and with no other associated information.
	 * @param x the x position.
	 * @param y the y position.
	 * @return the created point object.
	 */
	public static Point onlyAt(double x, double y) {
		return new Point(x, y, 0);
	}

	public static Point ORIGIN = new Point(0, 0);
}