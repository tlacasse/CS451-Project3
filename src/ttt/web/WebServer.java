package ttt.web;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

import ttt.Code;
import ttt.Game;
import ttt.agents.ServerBase;
import ttt.agents.SocketSide;
import ttt.learning.GameIO;

//Server deals with the C# web server, not the actual webpage client
final class WebServer extends ServerBase {

	public static final int PORT = 98;

	public static void main(String[] args) throws IOException {
		try (WebServer s = new WebServer()) {
			s.collectClients();
			s.start();
		} catch (IOException ioe) {
			// catch to close server
			throw ioe;
		}
	}

	private final Semaphore mutex;
	private boolean havePlayers;
	private int totalPlayers;

	private WebServer() throws IOException {
		super(new Game(-1), PORT); // set game player count later
		server.setSoTimeout(1000);

		totalPlayers = 0;
		havePlayers = false;

		mutex = new Semaphore(1);
	}

	private void collectClients() throws IOException {
		final Semaphore lock = new Semaphore(0);
		final Thread listener = new Thread(new Listener(lock));
		listener.start();
		try {
			lock.acquire(); // semaphore used just for waiting
			waitForStart();
			havePlayers = true;
			listener.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	private void waitForStart() throws IOException, InterruptedException {
		for (;;) {
			mutex.acquire();
			for (Client client : clients) {
				if (client.available() > 0) {
					client.readByte(); // GAME_START
					mutex.release();
					return;
				}
			}
			mutex.release();
		}
	}

	private void start() throws IOException {
		game.setPlayerCount(totalPlayers);
		try {
			for (;; turn = (turn + 1) % totalPlayers) {
				processClient();
			}
		} catch (EndGameException ege) {
		}
		GameIO.saveGame(game, true);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private final class Listener implements Runnable {

		final Semaphore lock;

		public Listener(Semaphore lock) {
			this.lock = lock;
		}

		@Override
		public void run() {
			while (!havePlayers) {
				try {
					WebClient client = new WebClient(); // may or may not
														// connect
					if (client.connected) {
						mutex.acquire();
						clients.offer(client);
						mutex.release();
						totalPlayers++;
						if (totalPlayers == 1) {
							client.writeByte(Code.FIRST_PLAYER);
							client.flush();
							lock.release();
						} else {
							client.writeByte(Code.CONNECTED);
							client.flush();
						}
					}
				} catch (IOException | InterruptedException ioeie) {
					System.out.println(ioeie);
				}
			}
		}
	}

	private final class WebClient extends SocketSide implements Client {

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
