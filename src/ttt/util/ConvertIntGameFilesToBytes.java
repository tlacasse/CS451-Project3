package ttt.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Deprecated // it has been used
final class ConvertIntGameFilesToBytes {

	private ConvertIntGameFilesToBytes() {
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		final File directory = new File(GameIO.DIRECTORY_GAMES);
		for (File file : directory.listFiles()) {
			// +1 because one short
			final ByteBuffer buffer = ByteBuffer.allocate((int) ((file.length() / Integer.BYTES) + 1));
			try (FileInputStream fis = new FileInputStream(file); DataInputStream reader = new DataInputStream(fis)) {
				buffer.put(TTTUtil.checkRange(reader.readInt())); // player
				buffer.put(TTTUtil.checkRange(reader.readInt())); // winner
				final short count = (short) reader.readInt();
				buffer.putShort(count); // count
				for (int i = 0; i < count * 3; i++) {
					buffer.put(TTTUtil.checkRange(reader.readInt())); // moves
				}
			}
			try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath())) {
				fos.write(buffer.array());
			}
		}
	}

}
