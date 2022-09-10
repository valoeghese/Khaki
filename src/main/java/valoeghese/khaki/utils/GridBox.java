package valoeghese.khaki.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A big box made up of a grid of little boxes, which each hold a set of data of a given type.
 * @param <T> the type of data stored in each little box.
 */
public class GridBox<T> {
	/**
	 * Constructs a new GridBox with the given parameters.
	 * @param boxSize the size of the box.
	 * @param squareRadius the number of little boxes extended in each cardinal direction to form the big box.
	 */
	public GridBox(int boxSize, int squareRadius, int offsetX, int offsetY) {
		this.boxes = new Set[squareRadius * 2][squareRadius * 2];
		this.boxSize = boxSize;
		this.boxShift = squareRadius;

		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	private final Collection<T> empty = List.of();
	private final Set<T>[][] boxes;
	private final int boxSize;
	private final int boxShift;
	private final int offsetX;
	private final int offsetY;

	public int getWidth() {
		return this.boxes.length;
	}

	public int getHeight() {
		return this.boxes[0].length;
	}

	public int getSquareRadius() {
		return this.boxShift;
	}

	public int getBoxSize() {
		return this.boxSize;
	}

	// if you use this to modify the data you are cringe
	public Collection<T> get(int x, int y) {
		// convert to grid space
		x = ((x + this.offsetX) / boxSize) + this.boxShift;
		y = ((y + this.offsetY) / boxSize) + this.boxShift;

		try {
			Collection<T> r = boxes[x][y];
			return r == null ? this.empty : r;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return this.empty;
		}
	}

	// if you use this to modify the data you are cringe
	public Collection<T> getGridBox(int gridX, int gridY) {
		try {
			Collection<T> r = boxes[gridX][gridY];
			return r == null ? this.empty : r;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return this.empty;
		}
	}

	public int gridSpaceX(int x) {
		return ((x + this.offsetX) / boxSize) + this.boxShift;
	}

	public int gridSpaceY(int y) {
		return ((y + this.offsetY) / boxSize) + this.boxShift;
	}

	public int getOffsetX() {
		return this.offsetX;
	}

	public int getOffsetY() {
		return this.offsetY;
	}

	public void add(int x, int y, T item) throws ArrayIndexOutOfBoundsException {
		// convert to grid space
		x = ((x + this.offsetX) / boxSize) + this.boxShift;
		y = ((y + this.offsetY) / boxSize) + this.boxShift;

		if (boxes[x][y] == null) boxes[x][y] = new HashSet<>();
		boxes[x][y].add(item);
	}

	public void put(int gridX, int gridY, Set<T> items) throws ArrayIndexOutOfBoundsException {
		boxes[gridX][gridY] = items;
	}

	public boolean containsInGridBox(int gridX, int gridY, T item) {
		return this.getGridBox(gridX, gridY).contains(item);
	}

	@Nullable
	public Collection<T> removeAll(int x, int y) throws ArrayIndexOutOfBoundsException {
		// convert to grid space
		x = ((x + this.offsetX) / boxSize) + this.boxShift;
		y = ((y + this.offsetY) / boxSize) + this.boxShift;

		Collection<T> original = boxes[x][y];
		boxes[x][y] = null;
		return original;
	}

	public void remove(int x, int y, T item) throws ArrayIndexOutOfBoundsException {
		// convert to grid space
		x = ((x + this.offsetX) / boxSize) + this.boxShift;
		y = ((y + this.offsetY) / boxSize) + this.boxShift;

		@Nullable Set<T> set = boxes[x][y];
		if (set != null) set.remove(item);
	}

	public Set<T>[][] toArray() {
		// deep copy
		Set<T>[][] result = new Set[this.getWidth()][this.getHeight()];

		for (int i = 0; i < this.boxes.length; i++) {
			result[i] = new Set[this.getHeight()];
			System.arraycopy(this.boxes[i], 0, result[i], 0, this.getHeight());
		}

		return result;
	}

	/**
	 * Constructs a frame for this grid box. All add/remove operations on the frame affect the parent grid box.
	 * However, operations merely retrieving data, namely get and toArray, only affect elements added to this frame.
	 *
	 * @return a frame of this grid box.
	 * @apiNote yes, you can make a frame of a frame.
	 */
	public GridBox<T> createFrame() {
		return new Frame();
	}

	/**
	 * A frame of a grid box.
	 * All add/remove operations on this frame affect the parent gridbox, however get and toArray only affect elements added to this frame.
	 *
	 * Yes you can make a frame of a frame.
	 */
	private class Frame extends GridBox<T> {
		private Frame() {
			super(GridBox.this.getBoxSize(), GridBox.this.getSquareRadius(), GridBox.this.offsetX, GridBox.this.offsetY);
		}

		@Override
		public void add(int x, int y, T item) throws ArrayIndexOutOfBoundsException {
			GridBox.this.add(x, y, item);
			super.add(x, y, item);
		}

		@Override
		public void put(int gridX, int gridY, Set<T> items) throws ArrayIndexOutOfBoundsException {
			GridBox.this.put(gridX, gridY, items);
			super.put(gridX, gridY, items);
		}

		@Override
		public void remove(int x, int y, T item) throws ArrayIndexOutOfBoundsException {
			GridBox.this.remove(x, y, item);
			super.remove(x, y, item);
		}

		@Override
		public @Nullable Collection<T> removeAll(int x, int y) throws ArrayIndexOutOfBoundsException {
			GridBox.this.removeAll(x, y);
			return super.removeAll(x, y);
		}
	}
}
