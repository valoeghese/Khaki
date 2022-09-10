package valoeghese.strom;

import org.jetbrains.annotations.Nullable;
import valoeghese.strom.utils.GridBox;
import valoeghese.strom.utils.Point;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Pregenerated Continent Data.
 *
 * @param centre the centre point of the continent, in blocks.
 * @param mountains the position of mountain peaks in a range on the continent.
 * @param rivers the paths of rivers in the continent.
 */
public record ContinentData(Point centre, Point[] mountains, GridBox<Node> rivers) {
	public void write(File file) throws IOException {
		try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))))) {
			dos.writeInt(0xA3A);
			dos.writeByte(0);

			// write centre
			centre.write(dos);

			// write mountains
			dos.writeInt(mountains.length);
			for (Point p : mountains) p.write(dos);

			// write rivers
			dos.writeInt(rivers.getBoxSize());
			dos.writeInt(rivers.getSquareRadius());
			dos.writeInt(rivers.getOffsetX());
			dos.writeInt(rivers.getOffsetY());

			for (Set<Node>[] column : rivers.toArray()) {
				for (@Nullable Set<Node> entry : column) {
					if (entry == null) {
						dos.writeInt(0);
					}
					else {
						dos.writeInt(entry.size());
						for (Node n : entry) n.write(dos);
					}
				}
			}
		}
	}

	public static ContinentData read(File file) throws IOException, IllegalStateException {
		try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))) {
			int magic = dis.readInt();

			if (magic != 0xA3A) { // awa
				throw new IllegalStateException("Invalid Format: This is not a Pregenerated Continent Data File");
			}

			byte version = dis.readByte();

			if (version > 0) {
				throw new IllegalStateException("Unsupported Format Version: " + version);
 			}

			// centre
			Point centre = Point.read(dis);

			// mountains
			Point[] mountains = new Point[dis.readInt()];

			for (int i = 0; i < mountains.length; i++) {
				mountains[i] = Point.read(dis);
			}

			// rivers
			int boxSize = dis.readInt();
			int sqrRad = dis.readInt();
			int ox = dis.readInt();
			int oy = dis.readInt();

			GridBox<Node> rivers = new GridBox<>(boxSize, sqrRad, ox, oy);

			for (int x = 0; x < sqrRad * 2; x++) {
				for (int y = 0; y < sqrRad * 2; y++) {
					int size = dis.readInt();

					if (size > 0) {
						Set<Node> riverNodes = new HashSet<>();

						for (int i = 0; i < size; i++) {
							riverNodes.add(Node.read(dis));
						}

						rivers.put(x, y, riverNodes);
					}
				}
			}

			return new ContinentData(centre, mountains, rivers);
		}
	}
}
