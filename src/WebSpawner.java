import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

final class WebSpawner {

	public static final int PORT_OPEN = 97;
	public static final int PORT_CLOSE = 96;

	public static void main(String[] args) throws IOException {
		// https://stackoverflow.com/questions/5747803/running-code-on-program-exit-in-java
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("SHUTDOWN");
				if (process != null) {
					process.destroy();
				}
				for (ServerSocket s : servers) {
					try {
						s.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		final Thread open = new Thread(new Wait(PORT_OPEN) {
			@Override
			public void process() throws IOException {
				if (!serverIsRunning) {
					process = (new ProcessBuilder("java", "-cp", "C:\\Users\\XYZ\\workspace\\CS451-Project3\\bin",
							"ttt.web.WebServer")).start();
					System.out.println(process);
					serverIsRunning = true;
				}
			}
		});
		final Thread close = new Thread(new Wait(PORT_CLOSE) {
			@Override
			public void process() throws IOException {
				if (serverIsRunning) {
					System.out.println("KILL");
					process.destroy();
					serverIsRunning = false;
				}
			}
		});
		open.start();
		close.start();
	}

	private static Process process = null;
	private static boolean serverIsRunning = false;
	private static List<ServerSocket> servers = new LinkedList<>();

	private static abstract class Wait implements Runnable {

		protected final ServerSocket server;

		Wait(int port) throws IOException {
			server = new ServerSocket(port);
			System.out.println(server);
			servers.add(server);
		}

		@Override
		public void run() {
			try {
				for (;;) {
					Socket socket = server.accept();
					System.out.println(socket);
					process();
					socket.close();
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
