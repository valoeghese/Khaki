package valoeghese.strom;

import valoeghese.strom.utils.Point;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A node in the river generator for interpolation between two points.
 * @param previous the previous point which this node connects to, can be NONE.
 * @param current the next point which this node connects to, cannot be NONE.
 */
public record Node(Point previous, Point current) {
	public void write(DataOutput out) throws IOException {
		previous.write(out);
		current.write(out);
	}

	public static Node read(DataInput in) throws IOException {
		return new Node(Point.read(in), Point.read(in));
	}
}
