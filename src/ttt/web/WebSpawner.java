package ttt.web;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ttt.util.GameIO;

/**
 * Starts and stops the website game server. Uses multiple server sockets to not
 * have to deal with sending data. Client sockets join and then are closed.
 */
final class WebSpawner {

	public static final int PORT_OPEN = 97;
	public static final int PORT_CLOSE = 96;
	public static final int PORT_OPEN_AI = 95;

	private static Process process = null;
	private static boolean serverIsRunning = false;
	private static List<ServerSocket> servers = new LinkedList<>();
	private static List<Thread> threads = new LinkedList<>();
	private static boolean done = false;

	public static void main(String[] args) throws IOException {
		// https://stackoverflow.com/questions/5747803/running-code-on-program-exit-in-java
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownThread()));
		start(new StartingWait(PORT_OPEN));
		start(new StartingWait(PORT_OPEN_AI, "1"));
		start(new EndingWait(PORT_CLOSE));
	}

	private static void print(Object obj) {
		System.out.println("WebSpawner\t" + String.valueOf(obj));
	}

	private static void start(Wait r) {
		Thread t = new Thread(r);
		threads.add(t);
		t.start();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final ArrayList<String> BASEARGS = new ArrayList<>();

	static {
		BASEARGS.add("java");
		BASEARGS.add("-cp");
		BASEARGS.add(GameIO.BIN);
		BASEARGS.add("ttt.web.WebServer");
	}

	private static final class StartingWait extends Wait {

		final ArrayList<String> cmdArgs;

		StartingWait(int port, String... args) throws IOException {
			super(port);
			cmdArgs = new ArrayList<>();
			cmdArgs.addAll(BASEARGS);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					cmdArgs.add(args[i]);
				}
			}
		}

		@Override
		void process() throws IOException {
			if (!serverIsRunning || !process.isAlive()) {
				process = (new ProcessBuilder(cmdArgs)).inheritIO().start();
				print(process);
				serverIsRunning = true;
			}
		}

	}

	private static final class EndingWait extends Wait {

		EndingWait(int port) throws IOException {
			super(port);
		}

		@Override
		void process() throws IOException {
			if (serverIsRunning) {
				print("KILL");
				process.destroy();
				serverIsRunning = false;
			}
		}

	}

	private static abstract class Wait implements Runnable {

		protected final ServerSocket server;

		Wait(int port) throws IOException {
			server = new ServerSocket(port);
			server.setSoTimeout(30000);
			print(server);
			servers.add(server);
		}

		@Override
		public void run() {
			try {
				while (!done) {
					try {
						Socket socket = server.accept();
						print(socket);
						process();
						socket.close();
					} catch (SocketTimeoutException ste) {
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		abstract void process() throws IOException;

	}

	private static final class ShutDownThread implements Runnable {

		@Override
		public void run() {
			System.out.println("WebSpawner SHUTDOWN");
			done = true;
			if (process != null) {
				process.destroy();
			}
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			for (ServerSocket s : servers) {
				try {
					s.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

	}

	private WebSpawner() {
	}

}
