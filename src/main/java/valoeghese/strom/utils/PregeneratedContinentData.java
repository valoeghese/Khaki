package valoeghese.strom.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Pregenerated Continent Data.
 *
 * @param centre the centre point of the continent, in metres.
 * @param mountains the position of mountain peaks in a range on the continent.
 * @param rivers the paths of rivers in the continent.
 */
public record PregeneratedContinentData(Point centre, Point[] mountains, List<Point[]> rivers) {
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
			dos.writeInt(rivers.size());

			for (Point[] ps : rivers) {
				dos.writeInt(ps.length);
				for (Point p : ps) p.write(dos);
			}
		}
	}

	public static PregeneratedContinentData read(File file) throws IOException, IllegalStateException {
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
			int riverCount = dis.readInt();
			List<Point[]> rivers = new ArrayList<>(riverCount);

			for (int i = 0; i < riverCount; i++) {
				Point[] river = new Point[dis.readInt()];

				for (int j = 0; j < river.length; j++) {
					river[j] = Point.read(dis);
				}

				rivers.add(river);
			}

			return new PregeneratedContinentData(centre, mountains, rivers);
		}
	}
}
