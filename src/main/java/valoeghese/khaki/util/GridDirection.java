package valoeghese.khaki.util;

import valoeghese.khaki.gen.KhakiNoiseGenerator.GridShape;

public enum GridDirection {
	UP(0, 0, 1, false),
	RIGHT(1, 1, 0, true),
	DOWN(2, 0, -1, false),
	LEFT(3, -1, 0, true);

	private GridDirection(int id, int xOff, int zOff, boolean horizontal) {
		this.id = id;
		this.xOff = xOff;
		this.zOff = zOff;
		this.horizontal = horizontal;
	}

	public final int id;
	public final int xOff, zOff;
	public final boolean horizontal;

	public GridDirection reverse() {
		switch (this) {
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		default:
			return null;
		}
	}

	public static void deserialise(GridDirection[] directions, int number) {
		int iShape = number & 0b11;
		number >>= 2;
		number &= 0b11;

		directions[0] = BY_ID[number];

		GridShape shape = GridShape.BY_ID[iShape];

		switch (shape) {
		case ANTICLOCKWISE:
			directions[1] = BY_ID[(number - 1) & 0b11];
			break;
		case CLOCKWISE:
			directions[1] = BY_ID[(number + 1) & 0b11];
			break;
		case LINE:
			directions[1] = directions[0].reverse();
			break;
		case NODE:
			directions[1] = directions[0];
			break;
		default:
			throw new NullPointerException("Impossible error. Notify me (valoeghese) Immediately!. Debug Data: IShape = " + iShape + ", Direction = " + number + ", Array Size = " + directions.length);
		}
	}

	public static int serialise(GridDirection from, GridDirection to) {
		// both cannot be null.
		int result = 0;

		if (from == null) {
			result |= to.id;
			result <<= 2;
			result |= GridShape.NODE.id;
		} else if (to == null) {
			result |= from.id;
			result <<= 2;
			result |= GridShape.NODE.id;
		} else if (from == to.reverse()) {
			result |= from.id;
			result <<= 2;
			result |= GridShape.LINE.id;
		} else {
			result |= from.id;
			result <<= 2;
			result |= ((from.id + 1) & 0b11) == to.id ? GridShape.CLOCKWISE.id : GridShape.ANTICLOCKWISE.id;
		}

		return result;
	}

	public static final GridDirection[] BY_ID = new GridDirection[4];

	static {
		for (GridDirection d : GridDirection.values()) {
			BY_ID[d.id] = d;
		}
	}
}