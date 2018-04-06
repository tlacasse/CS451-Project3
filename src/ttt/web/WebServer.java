package ttt.web;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

import ttt.Code;
import ttt.Game;
import ttt.agents.ServerBase;
import ttt.agents.SocketSide;

//Server deals with the C# web server, not the actual webpage client
final class WebServer extends ServerBase {

	public static final int PORT = 98;

	@SuppressWarnings("resource") // IDE can't figure out that this is being
									// closed
	public static void main(String[] args) throws IOException {
		(new WebServer()).collectClients().start().close();
	}

	private boolean havePlayers;
	private int totalPlayers;

	private WebServer() throws IOException {
		super(new Game(-1), PORT); // set game player count later
		server.setSoTimeout(1000);

		totalPlayers = 0;
		havePlayers = false;
	}

	private WebServer collectClients() throws IOException {
		final Semaphore lock = new Semaphore(0);
		final Thread listener = new Thread(new Listener(lock));
		listener.start();
		try {
			lock.acquire(); // semaphore used just for waiting
			Client first = clients.peek();
			first.writeByte(Code.FIRST_PLAYER);
			first.flush();
			first.readByte(); // doesn't matter what it is
			havePlayers = true;
			listener.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		return this;
	}

	private WebServer start() throws IOException {
		game.setPlayerCount(totalPlayers);
		try {
			for (;; turn = (turn + 1) % totalPlayers) {
				processClient();
			}
		} catch (EndGameException ege) {
		}
		return this;
	}

	private final class Listener implements Runnable {

		private final Semaphore lock;

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
						clients.offer(client);
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
				} catch (IOException ioe) {
					System.out.println(ioe);
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
