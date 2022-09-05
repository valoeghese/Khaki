package valoeghese.strom.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Point {
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
		this.height = 0;
		this.flags = 0;
	}

	public Point(double x, double y, double height) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.flags = 0;
	}

	public Point(double x, double y, double height, int flags) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.flags = flags;
	}

	private final double x;
	private final double y;
	private final double height;
	private final int flags;

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getHeight() {
		return this.height;
	}

	public int getFlags() {
		return this.flags;
	}

	public Point add(double x, double y) {
		return new Point(this.x + x, this.y + y);
	}

	public Point add(double x, double y, int newValue) {
		return new Point(this.x + x, this.y + y, newValue);
	}

	public Point add(Point other) {
		return this.add(other.x, other.y);
	}

	public Point mul(double by) {
		return new Point(this.x * by, this.y * by, this.height, this.flags);
	}

	public Point lerp(double progress, Point to) {
		return new Point(
				Maths.lerp(progress, this.x, to.x),
				Maths.lerp(progress, this.y, to.y)
		);
	}

	public Point lerph(double progress, Point to) {
		return new Point(
				Maths.lerp(progress, this.x, to.x),
				Maths.lerp(progress, this.y, to.y),
				Maths.lerp(progress, this.height, to.height),
				0
		);
	}

	public Point withValue(int value) {
		return new Point(this.x, this.y, value);
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
		result = 31 * result + Double.hashCode(this.x);
		result = 31 * result + Double.hashCode(this.y);
		result = 31 * result + Double.hashCode(this.x);
		return result;
	}

	@Override
	public String toString() {
		return "(" + this.x
				+ ", " + this.y
				+ ')';
	}

	public void write(DataOutput out) throws IOException {
		out.writeDouble(this.x);
		out.writeDouble(this.y);
		out.writeDouble(this.height);
		out.writeInt(this.flags);
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

	public static Point read(DataInput in) throws IOException {
		return new Point(in.readDouble(), in.readDouble(), in.readDouble(), in.readInt());
	}

	public static Point ORIGIN = new Point(0, 0);
	public static Point NONE = new Point(Double.NaN, Double.NaN);
}