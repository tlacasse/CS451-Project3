package ttt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public abstract class SocketSide implements AutoCloseable {

	protected static final String LOCALHOST = "127.0.0.1";

	protected Socket socket;

	private final Buffer buffer;
	private final InputStream in;
	private final OutputStream out;
	private final Scanner reader;

	public SocketSide(int port, int bufferSize) throws IOException {
		connect(port);
		in = socket.getInputStream();
		out = socket.getOutputStream();

		reader = new Scanner(in);

		buffer = new Buffer(bufferSize);
	}

	public SocketSide(int bufferSize) throws IOException {
		this(-1, bufferSize);
	}

	protected abstract void connect(int port);

	@Override
	public void close() throws Exception {
		reader.close();
		socket.close();
	}

}
