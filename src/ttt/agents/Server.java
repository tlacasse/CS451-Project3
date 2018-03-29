package ttt.agents;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;

import ttt.Board;
import ttt.Code;
import ttt.Game;

public class Server implements AutoCloseable, Runnable {

	private final ServerSocket server;
	private final Queue<Client> clients;
	private final Board board;
	private final Game game;
	private final int totalPlayers;

	private int turn;

	public Server(Game game, int port, int players) throws IOException {
		this.game = game;
		totalPlayers = players;

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
				clients.offer(new Client());
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
					final boolean isFull = board.isFull();
					if (isWin || isFull) {
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

	// private static int clientIdInc = 0;

	private class Client extends SocketSide {

		// private final int id;

		public Client() throws IOException {
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

}
