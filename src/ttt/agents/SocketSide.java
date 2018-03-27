package ttt.agents;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public abstract class SocketSide implements AutoCloseable {

	protected Socket socket;

	private final ByteBuffer buffer;
	private final InputStream in;
	private final OutputStream out;
	private final DataInputStream reader;

	public SocketSide(int port, int bufferSize) throws IOException {
		connect(port);
		in = socket.getInputStream();
		out = socket.getOutputStream();

		reader = new DataInputStream(in);

		buffer = ByteBuffer.allocate(bufferSize);
	}

	public SocketSide(int bufferSize) throws IOException {
		this(-1, bufferSize);
	}

	protected abstract void connect(int port) throws IOException;

	@Override
	public void close() throws IOException {
		reader.close();
		socket.close();
	}

	public void send() throws IOException {
		out.write(buffer.array());
		buffer.clear();
	}

	// writing

	public void writeInt(int x) {
		buffer.putInt(x);
	}

	public void writeByte(byte x) {
		buffer.put(x);
	}

	public void writeBoolean(boolean x) {
		buffer.put((byte) (x ? 1 : 0));
	}

	// reading blocks until data exists

	public int readInt() throws IOException {
		return reader.readInt();
	}

	public byte readByte() throws IOException {
		return reader.readByte();
	}

	public boolean readBoolean() throws IOException {
		return reader.readByte() == 1;
	}

}
