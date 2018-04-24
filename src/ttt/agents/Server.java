package ttt.agents;

import static ttt.Program.HAVE_USER;
import static ttt.Program.PLAYERS;

import java.io.IOException;
import java.util.Scanner;

import ttt.Board;
import ttt.Code;
import ttt.Config;
import ttt.Game;

/**
 * The Tic Tac Toe game server.
 */
public class Server extends ServerBase implements AutoCloseable, Runnable {

	private final Config config;
	private final int totalPlayers;

	public Server(Game game, int port, Config config) throws IOException {
		super(game, port);
		this.config = config;
		totalPlayers = config.get(PLAYERS);
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < totalPlayers; i++) {
				clients.offer(i == 0 && config.get(HAVE_USER) > 0 ? new ClientUser() : new ClientSocket());
			}
			System.out.println();
			for (;; turn = (turn + 1) % totalPlayers) {
				processClient();
			}
		} catch (EndGameException ege) {
		} catch (IOException | UnsupportedOperationException ioeuoe) {
			System.out.println("Thread Failed: " + this);
			ioeuoe.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	// normal IPC Player Client
	private class ClientSocket extends SocketSide implements Client {

		public ClientSocket() throws IOException {
			super();
		}

		@Override
		protected void connect(int port) throws IOException {
			if (socket == null || socket.isClosed()) {
				socket = server.accept();
				System.out.println(socket);
			}
		}

	}

	// Client with User Input
	private class ClientUser implements Client {

		final Scanner scan;
		boolean askSecond;
		boolean display;
		int save;

		public ClientUser() {
			this.scan = config.getScanner();
			askSecond = false;
			display = false;
			save = -1;
		}

		@Override
		public void close() throws IOException {
			// nothing
		}

		@Override
		public void flush() throws IOException {
			if (display) {
				System.out.println(board);
				display = false;
			}
		}

		@Override
		public void writeInt(int x) throws IOException {
			// nothing
		}

		@Override
		public void writeByte(byte x) throws IOException {
			if (x == Code.TURN) {
				display = true;
			}
		}

		@Override
		public int readInt() throws IOException {
			// switch x & y
			// board has first coord as row and second as column,
			// want to match normal graph order
			if (askSecond) {
				askSecond = !askSecond;
				return save;
			}
			for (boolean useX : new boolean[] { true, false }) {
				System.out.println("??? Input your move:");
				int value = -1;
				while (value < 0) {
					try {
						System.out.println(useX ? "?? Input x:" : "?? Input y:");
						// not scan.nextInt()
						// to make sure we are dealing with lines
						value = Integer.parseInt(scan.nextLine());
					} catch (NumberFormatException nfe) {
						System.out.println(useX ? "?? Input x:" : "?? Input y:");
					}
					if (value > Board.SIZE - 1) {
						value = -1;
					}
				}
				if (useX) {
					save = value;
				} else {
					askSecond = !askSecond;
					return value;
				}
			}
			return -1; // won't get here
		}

		@Override
		public byte readByte() throws IOException {
			return Code.MOVE;
		}

		@Override
		public int available() throws IOException {
			return 1;
		}

	}

}
