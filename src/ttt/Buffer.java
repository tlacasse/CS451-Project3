package ttt;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Buffer {

	private final ByteBuffer buffer;

	public Buffer(int size) {
		buffer = ByteBuffer.allocate(size);
	}

	public void send(OutputStream stream) throws IOException {
		stream.write(buffer.array());
		buffer.clear();
	}

	public void write(int i) {
		buffer.putInt(i);
	}

}
