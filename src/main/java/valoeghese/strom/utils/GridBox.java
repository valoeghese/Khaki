package valoeghese.strom.utils;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * A big box made up of a grid of little boxes, which each hold a list of data of a given type.
 * @param <T> the type of data stored in each little box.
 */
public class GridBox<T> {
	/**
	 * Constructs a new GridBox with the given parameters.
	 * @param boxSize the size of the box.
	 * @param squareRadius the number of little boxes extended in each cardinal direction to form the big box.
	 */
	public GridBox(int boxSize, int squareRadius) {
		this.boxes = new List[squareRadius * 2][squareRadius * 2];
		this.boxSize = boxSize;
		this.boxShift = squareRadius;
	}

	private final Iterable<T> empty = List.of();
	private final List<T>[][] boxes;
	private final int boxSize;
	private final int boxShift;

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
	public Iterable<T> get(int x, int y) {
		// convert to grid space
		x = (x / boxSize) + this.boxShift;
		y = (y / boxSize) + this.boxShift;

		try {
			Iterable<T> r = boxes[x][y];
			return r == null ? this.empty : r;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return this.empty;
		}
	}

	// if you use this to modify the data you are cringe
	public Iterable<T> getGridBox(int gridX, int gridY) {
		try {
			Iterable<T> r = boxes[gridX][gridY];
			return r == null ? this.empty : r;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return this.empty;
		}
	}

	public int gridSpace(int v) {
		return (v / boxSize) + this.boxShift;
	}

	public void add(int x, int y, T item) throws ArrayIndexOutOfBoundsException {
		// convert to grid space
		x = (x / boxSize) + this.boxShift;
		y = (y / boxSize) + this.boxShift;

		if (boxes[x][y] == null) boxes[x][y] = new LinkedList<>();
		boxes[x][y].add(item);
	}

	public void put(int gridX, int gridY, List<T> item) throws ArrayIndexOutOfBoundsException {
		boxes[gridX][gridY] = item;
	}

	@Nullable
	public List<T> remove(int x, int y) throws ArrayIndexOutOfBoundsException {
		// convert to grid space
		x = (x / boxSize) + this.boxShift;
		y = (y / boxSize) + this.boxShift;

		List<T> original = boxes[x][y];
		boxes[x][y] = null;
		return original;
	}

	public List<T>[][] toArray() {
		List<T>[][] result = new List[this.getWidth()][this.getHeight()];

		for (int i = 0; i < this.boxes.length; i++) {
			result[i] = new List[this.getHeight()];
			System.arraycopy(this.boxes[i], 0, result[i], 0, this.getHeight());
		}

		return result;
	}
}
