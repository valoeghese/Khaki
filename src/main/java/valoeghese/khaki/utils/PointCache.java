package valoeghese.khaki.utils;

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

	public T sample(Point point) {
		int loc = point.hashCode() & this.mask;

		if (!point.equals(this.positions[loc])) {
			//System.out.println("NEW LOC " + point);
			this.positions[loc] = point;
			return (T) (this.values[loc] = this.operator.sample(point));
		} else {
			return (T) this.values[loc];
		}
	}

	@FunctionalInterface
	public interface Operator<T> {
		T sample(Point point);
	}
}