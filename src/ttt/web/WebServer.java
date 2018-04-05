package ttt.web;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import ttt.Board;
import ttt.Code;
import ttt.Game;
import ttt.agents.ServerLoop;
import ttt.agents.SocketSide;

//Server deals with the C# web server, not the actual webpage client
final class WebServer extends ServerLoop {

	public static final int PORT = 98;

	public static void main(String[] args) throws IOException {
		new WebServer();
	}

	private boolean havePlayers = false;
	private final Semaphore lock;

	private final ServerSocket server;
	private final Queue<WebClient> clients;

	private int totalPlayers;

	private WebServer() throws IOException {
		server = new ServerSocket(PORT);
		server.setSoTimeout(1000);
		System.out.println(server);

		clients = new LinkedList<>();
		totalPlayers = 0;

		lock = new Semaphore(0);
		final Thread listener = new Thread(new Listener());

		try {
			lock.acquire();
			WebClient first = clients.peek();
			first.readByte(); // doesn't matter what it is
			for (WebClient client : clients) {
				client.writeByte(Code.START_GAME);
				client.flush();
			}
			listener.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

		final Game game = new Game(totalPlayers);
		final Board board = new Board(true);
		int turn = 0;

		try {
			for (;; turn = (turn + 1) % totalPlayers) {
				processClient(game, board, clients, turn);
				havePlayers = true;
			}
		} catch (EndGameException ege) {
		}

		for (WebClient client : clients) {
			client.close();
		}
		server.close();
	}

	private final class Listener implements Runnable {
		@Override
		public void run() {
			while (!havePlayers) {
				try {
					WebClient client = new WebClient();
					if (client.connected) {
						clients.offer(client);
						totalPlayers++;
						if (totalPlayers == 1) {
							client.writeByte(Code.FIRST_PLAYER);
							client.flush();
							lock.release();
						}
					}
				} catch (IOException ioe) {
					System.out.println(ioe);
				}
			}
		}
	}

	private final class WebClient extends SocketSide {

		boolean connected;

		public WebClient() throws IOException {
			super();
		}

		@Override
		protected void connect(int port) throws IOException {
			if (socket == null || socket.isClosed()) {
				try {
					socket = server.accept();
					System.out.println(socket);
					connected = true;
				} catch (SocketTimeoutException ste) {
					connected = false;
				}
			}
		}

	}
}
