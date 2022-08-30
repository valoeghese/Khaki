package valoeghese.strom.test;

public class ViewChunk {
	public ViewChunk(int x, int y) {
		this.x = x;
		this.y = y;
		this.startX = x << CHUNK_SHIFT;
		this.startY = y << CHUNK_SHIFT;
	}

	public static final int CHUNK_SHIFT = 8;
	public static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
	public static final int CHUNK_MOD = CHUNK_SIZE - 1;

	private final int[] COLOURS = new int[CHUNK_SIZE * CHUNK_SIZE];
	public final int x, y, startX, startY;
	public boolean shouldDestroy;

	public void set(int x, int y, int colour) {
		COLOURS[y * CHUNK_SIZE + x] = colour;
	}

	public int get(int x, int y) {
		return COLOURS[y * CHUNK_SIZE + x];
	}
}
