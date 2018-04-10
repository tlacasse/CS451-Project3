import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

final class WebSpawner {

	public static final int PORT_OPEN = 97;
	public static final int PORT_CLOSE = 96;

	private static Process process = null;
	private static boolean serverIsRunning = false;
	private static List<ServerSocket> servers = new LinkedList<>();
	private static List<Thread> threads = new LinkedList<>();
	private static boolean done = false;

	public static void main(String[] args) throws IOException {
		// https://stackoverflow.com/questions/5747803/running-code-on-program-exit-in-java
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("SHUTDOWN");
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
		});
		// trying this anonymous class concept :)
		start(new Wait(PORT_OPEN) {
			@Override
			public void process() throws IOException {
				if (!serverIsRunning || !process.isAlive()) {
					process = (new ProcessBuilder("java", "-cp", "C:\\Users\\XYZ\\workspace\\CS451-Project3\\bin",
							"ttt.web.WebServer")).inheritIO().start();
					print(process);
					serverIsRunning = true;
				}
			}
		});
		start(new Wait(PORT_CLOSE) {
			@Override
			public void process() throws IOException {
				if (serverIsRunning) {
					print("KILL");
					process.destroy();
					serverIsRunning = false;
				}
			}
		});
	}

	private static void print(Object obj) {
		System.out.println("WebSpawner\t" + String.valueOf(obj));
	}

	private static void start(Runnable r) {
		Thread t = new Thread(r);
		threads.add(t);
		t.start();
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

		public abstract void process() throws IOException;

	}

	private WebSpawner() {
	}

}
