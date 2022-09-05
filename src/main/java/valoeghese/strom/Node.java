package valoeghese.strom;

import org.jetbrains.annotations.Nullable;
import valoeghese.strom.utils.Point;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

/**
 * A node in the river generator for interpolation between two points.
 */
public class Node {
	/**
	 * @param previous the previous point which this node connects to, can be NONE.
	 * @param current the next point which this node connects to, cannot be NONE.
	 */
	public Node(Point previous, Point current) {
		this.previous = previous;
		this.current = current;
	}

	public Node(Point previous, Point current, int river) {
		this.previous = previous;
		this.current = current;
		this.river = river;
	}

	private final Point previous;
	private final Point current;
	// not saved, not used anywhere except shortly after assigned, in case a river is required to be redirected.
	@Nullable
	public transient Node next;
	public transient int river;

	public void write(DataOutput out) throws IOException {
		previous.write(out);
		current.write(out);
	}

	public static Node read(DataInput in) throws IOException {
		return new Node(Point.read(in), Point.read(in));
	}

	public Point previous() {
		return previous;
	}

	public Point current() {
		return current;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Node) obj;
		return Objects.equals(this.previous, that.previous) &&
				Objects.equals(this.current, that.current);
	}

	@Override
	public int hashCode() {
		return Objects.hash(previous, current);
	}

	@Override
	public String toString() {
		return "Node[" +
				"previous=" + previous + ", " +
				"current=" + current + ']';
	}
}
