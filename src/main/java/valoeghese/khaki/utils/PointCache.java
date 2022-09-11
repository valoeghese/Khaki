package valoeghese.khaki.utils;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Arrays;

/**
 * Fast cache that shortens the number of possible hash values via using an `original & mask` algorithm, and uses an array lookup.
 */
public class PointCache<T> {
	/**
	 * @param size the size of the cache. Must be a power of 2!
	 * @param operator the operation to cache.
	 */
	public PointCache(int size, Operator<T> operator) {
		this.operator = operator;
		this.mask = size - 1;
		this.positions = new Point[size];
		this.values = new Object[size];
	}

	private final Operator<T> operator;

	private int mask;
	private Point[] positions;
	private Object[] values;
	private Object lock = new Object();

	public T sample(Point point) {
		synchronized (lock) {
			int loc = point.hashCode() & this.mask;

			if (point.equals(this.positions[loc])) {
				System.out.printf("%s\tequals\t%s\n", point, this.positions[loc]);
				return (T) this.values[loc];
			} else {
				//System.out.println("NEW LOC " + point);
				this.positions[loc] = point;
				return (T) (this.values[loc] = this.operator.sample(point));
			}
		}
	}

	@VisibleForTesting
	private T nonNull(@Nullable T value) {
		if (value == null) {
			System.err.println(this);
			throw new NullPointerException("PointCache cannot return null!");
		}

		return value;
	}

	@Override
	public String toString() {
		return "PointCache{" +
				"operator=" + operator +
				", mask=" + mask +
				", positions=" + Arrays.toString(positions) +
				", values=" + Arrays.toString(values) +
				'}';
	}

	@FunctionalInterface
	public interface Operator<T> {
		T sample(Point point);
	}
}