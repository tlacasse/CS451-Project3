package ttt.agents;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public abstract class SocketSide implements AutoCloseable {

	protected Socket socket;

	private final ByteBuffer buffer;
	private final InputStream in;
	private final OutputStream out;
	private final Scanner reader;

	public SocketSide(int port, int bufferSize) throws IOException {
		connect(port);
		in = socket.getInputStream();
		out = socket.getOutputStream();

		reader = new Scanner(in);

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

	public void write(int x) {
		buffer.putInt(x);
	}

}
