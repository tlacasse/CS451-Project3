package ttt.agents;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import ttt.Board;
import ttt.Code;
import ttt.Config;
import ttt.Game;
import ttt.Param;

public class Server implements AutoCloseable, Runnable {

	private final ServerSocket server;
	private final Queue<Client> clients;
	private final Board board;
	private final Game game;
	private final Config config;
	private final int totalPlayers;

	private int turn;

	public Server(Game game, int port, Config config) throws IOException {
		this.game = game;
		this.config = config;
		totalPlayers = config.get(Param.PLAYERS);

		server = new ServerSocket(port);
		System.out.println(server);

		clients = new LinkedList<Client>();
		board = new Board(true);
		turn = 0;
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < totalPlayers; i++) {
				clients.offer(i == 0 && config.get(Param.HAVE_USER) > 0 ? new ClientUser() : new ClientWeb());
			}
			System.out.println();
			for (;; turn = (turn + 1) % totalPlayers) {
				final Client active = clients.poll();
				System.out.println("Turn: " + turn);
				active.writeByte(Code.TURN);
				active.flush();

				final byte mode = active.readByte();
				switch (mode) {
				case Code.MOVE:
					final int x = active.readInt();
					final int y = active.readInt();
					board.set(x, y, turn);
					game.recordMove(turn, x, y);
					System.out.println("Move: " + x + ", " + y);
					for (Client other : clients) {
						other.writeByte(Code.OTHER_PLAYER_MOVE);
						other.writeInt(x);
						other.writeInt(y);
						other.flush();
					}
					final boolean isWin = board.isWin(turn, x, y);
					if (isWin || board.isFull()) {
						final byte code = isWin ? Code.GAME_DONE : Code.FULL_BOARD;
						clients.offer(active);
						for (Client client : clients) {
							client.writeByte(code);
							client.flush();
						}
						if (isWin) {
							game.setWinner(turn);
							System.out.println("Winner: " + turn);
						}
						return;
					}
					break;
				default:
					throw new UnsupportedOperationException("Code: " + mode);
				}
				clients.offer(active);
			}
		} catch (IOException | UnsupportedOperationException e) {
			System.out.println("Thread Failed: " + this);
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		for (Client client : clients) {
			if (client != null) {
				client.close();
			}
		}
		server.close();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/* Nested classes to allow access to private Server variables. */

	// type representing Clients, to allow a "user" Client
	private interface Client extends SocketReadWrite {
	}

	// private static int clientIdInc = 0;

	// normal IPC Player Client
	private class ClientWeb extends SocketSide implements Client {

		// private final int id;

		public ClientWeb() throws IOException {
			super();
			// id = clientIdInc++;
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

		private final Scanner scan;
		private boolean askSecond;
		private int save;

		public ClientUser() {
			this.scan = config.getScanner();
			askSecond = false;
			save = -1;
		}

		@Override
		public void close() throws IOException {
			// nothing to close
		}

		@Override
		public void flush() throws IOException {
			System.out.println(board);
		}

		@Override
		public void writeInt(int x) throws IOException {
			// nothing to receive, everything is in Server class
		}

		@Override
		public void writeByte(byte x) throws IOException {
			// nothing to receive, everything is in Server class
		}

		@Override
		public int readInt() throws IOException {
			// switch x & y
			if (askSecond) {
				askSecond = !askSecond;
				return save;
			}
			for (boolean useX : new boolean[] { true, false }) {
				System.out.println("??? Input your move:");
				int value = -1;
				while (value == -1) {
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
			return (byte) Code.MOVE;
		}

	}

}
