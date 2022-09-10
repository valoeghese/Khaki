package valoeghese.khaki.utils;

import java.util.Arrays;

/**
 * Fast cache that shortens the number of possible hash values via using an `original & mask` algorithm, and uses an array lookup.
 */
public class SimpleCache<T> {
	/**
	 * @param size the size of the cache. Must be a power of 2!
	 * @param operator the operation to cache.
	 */
	public SimpleCache(int size, Operator<T> operator) {
		this.operator = operator;
		this.mask = size - 1;
		this.positions = new long[size];
		this.values = new Object[size];

		// It's pretty much never gonna be max value so this is a good "no value" template
		Arrays.fill(this.positions, Long.MAX_VALUE);
	}

	private final Operator<T> operator;

	private int mask;
	private long[] positions;
	private Object[] values;

	public T sample(int x, int z) {
		long pos = (long)x << 32 | (long)z;
		int loc = mix5(x, z) & this.mask;

		if (this.positions[loc] != pos) {
			this.positions[loc] = pos;
			return (T) (this.values[loc] = this.operator.sample(x, z));
		} else {
			return (T) this.values[loc];
		}
	}

	private static int mix5(int a, int b) {
		return (((a >> 4) & 1) << 9) |
				(((b >> 4) & 1) << 8) |
				(((a >> 3) & 1) << 7) |
				(((b >> 3) & 1) << 6) |
				(((a >> 2) & 1) << 5) |
				(((b >> 2) & 1) << 4) |
				(((a >> 1) & 1) << 3) |
				(((b >> 1) & 1) << 2) |
				((a & 1) << 1) |
				(b & 1);
	}

	@FunctionalInterface
	public interface Operator<T> {
		T sample(int x, int z);
	}
}