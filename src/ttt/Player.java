package ttt;

import java.io.IOException;
import java.net.Socket;

public class Player extends SocketSide implements AutoCloseable {

	public static int main(String[] args) {
		final int port = Integer.parseInt(args[0]);
		try (Player player = new Player(port)) {

		} catch (Exception e) {
			System.out.println("Failed: " + e);
			e.printStackTrace();
		}
		return 0;
	}

	public Player(int port) throws IOException {
		super(port, 2 * 4);
	}

	@Override
	protected void connect(int port) throws IOException {
		if (socket == null || socket.isClosed()) {
			socket = new Socket(LOCALHOST, port);
		}
	}

}
