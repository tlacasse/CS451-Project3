package ttt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Player implements AutoCloseable {

	public static int main(String[] args) {
		final int port = Integer.parseInt(args[0]);
		try (Player player = new Player(port)) {

		} catch (Exception e) {
			System.out.println("Failed: " + e);
			e.printStackTrace();
		}
		return 0;
	}

	private final Socket socket;
	private final Buffer buffer;
	private final InputStream in;
	private final OutputStream out;
	private final Scanner reader;

	public Player(int port) throws UnknownHostException, IOException {
		socket = new Socket("127.0.0.1", port);
		in = socket.getInputStream();
		out = socket.getOutputStream();

		reader = new Scanner(in);

		buffer = new Buffer();
	}

	@Override
	public void close() throws Exception {
		reader.close();
		socket.close();
	}

}
