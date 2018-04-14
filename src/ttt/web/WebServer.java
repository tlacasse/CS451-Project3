package ttt.web;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

import ttt.Code;
import ttt.Game;
import ttt.Program;
import ttt.agents.ServerBase;
import ttt.agents.SocketSide;
import ttt.agents.Spawn;
import ttt.learning.GameIO;
import ttt.learning.GamePostfix;

//Server deals with the C# web server, not the actual webpage client
final class WebServer extends ServerBase {

	public static final int PORT = 98;

	private static WebServer webServer = null;

	public static void main(String[] args) throws IOException {
		// https://stackoverflow.com/questions/5747803/running-code-on-program-exit-in-java
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownThread()));
		final boolean isPvP = (args == null || args.length == 0);
		try (WebServer ws = (webServer = new WebServer(isPvP))) {
			webServer.collectClients();
			webServer.start();
		}
	}

	private final Semaphore mutex;
	private final boolean isPvP;
	private boolean havePlayers;
	private int totalPlayers;
	private Thread listener, playerSpawn;

	private WebServer(boolean isPvP) throws IOException {
		super(new Game(-1), PORT); // set game player count later
		this.isPvP = isPvP;
		server.setSoTimeout(1000);

		totalPlayers = 0;
		havePlayers = false;

		mutex = new Semaphore(1);
		listener = playerSpawn = null;
	}

	private void collectClients() throws IOException {
		final Semaphore lock = new Semaphore(0);
		listener = new Thread(new Listener(lock));
		listener.start();
		try {
			lock.acquire(); // semaphore used just for waiting
			waitForStart();
			havePlayers = true;
			listener.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		listener = null;
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
		GameIO.saveGame(game, isPvP ? GamePostfix.PVP : GamePostfix.USER_VS_AI);
		playerSpawn = null;
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
							if (!isPvP) {
								(playerSpawn = Spawn.newPlayer(PORT)).start();
							}
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

	private static final class ShutDownThread implements Runnable {

		@Override
		public void run() {
			System.out.println("SHUTDOWN");
			webServer.havePlayers = true;
			if (webServer != null) {
				try {
					webServer.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				if (webServer.listener != null) {
					Program.join(webServer.listener);
				}
				if (webServer.playerSpawn != null) {
					Spawn.killPlayers();
					Program.join(webServer.playerSpawn);
				}
			}
		}

	}

}
