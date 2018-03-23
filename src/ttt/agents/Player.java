package ttt.agents;

import java.io.IOException;
import java.net.Socket;

import ttt.Board;

public class Player extends SocketSide implements AutoCloseable {

	private static final String LOCALHOST = "127.0.0.1";

	public static int main(String[] args) throws Exception {
		final int port = Integer.parseInt(args[0]);
		try (Player player = new Player(port)) {

		} catch (Exception e) {
			System.out.println("Failed!");
			e.printStackTrace();
		}
		return 0;
	}

	private final Board board;

	public Player(int port) throws IOException {
		super(port, 2 * 4);
		board = new Board();
	}

	@Override
	protected void connect(int port) throws IOException {
		if (socket == null || socket.isClosed()) {
			socket = new Socket(LOCALHOST, port);
		}
	}

}
