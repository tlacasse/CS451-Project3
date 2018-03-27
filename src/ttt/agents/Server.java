package ttt.agents;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;

import ttt.Board;
import ttt.Code;

public class Server implements AutoCloseable, Runnable {

	private final ServerSocket server;
	private final Queue<Client> clients;
	private final Board board;
	private final int totalPlayers;
	private int turn;

	public Server(int port, int players) throws IOException {
		totalPlayers = players;

		server = new ServerSocket(port);
		System.out.println(this);

		clients = new LinkedList<Client>();
		board = new Board();
		turn = 0;
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < totalPlayers; i++) {
				clients.add(new Client());
			}
			for (;; turn = (turn + 1) % totalPlayers) {
				final Client active = clients.poll();
				active.writeByte(Code.TURN);
				active.send();

				final byte mode = active.readByte();
				switch (mode) {
				case Code.MOVE:
					final int x = active.readInt();
					final int y = active.readInt();
					board.set(x, y, (byte) turn);
					for (Client other : clients) {
						other.writeByte(Code.OTHER_PLAYER_MOVE);
						other.writeInt(x);
						other.writeInt(y);
						other.send();
					}
					if (board.isWin((byte) turn, x, y)) {
						for (Client other : clients) {
							other.writeByte(Code.GAME_DONE);
							other.send();
						}
						return;
					}
					break;
				default:
					throw new UnsupportedOperationException();
				}

				clients.add(active);
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

	private static final int CLIENT_BUFFER_SIZE = 0;

	private static int clientIdInc = 0;

	private class Client extends SocketSide {

		private final int id;

		public Client() throws IOException {
			super(CLIENT_BUFFER_SIZE);
			id = clientIdInc++;
		}

		@Override
		protected void connect(int port) throws IOException {
			if (socket == null || socket.isClosed()) {
				socket = server.accept();
				System.out.println(this);
			}
		}

	}

}
