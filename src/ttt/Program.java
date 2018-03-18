package ttt;

import java.io.IOException;
import java.util.ArrayList;

public class Program {

	public static final int PORT = 6327;
	public static final int THREADS = 2;

	public static void main(String[] args) throws IOException, Exception {

	}

	private static void go() throws IOException, Exception {
		try (Server server = new Server(PORT, THREADS)) {
			final ArrayList<Thread> threads = new ArrayList<>();
			final Thread serverThread = new Thread(server);

			for (int i = 0; i < THREADS; i++) {
				threads.add(new Thread(new Spawn(PORT)));
			}

			serverThread.start();
			for (Thread thread : threads) {
				thread.start();
			}

			for (Thread thread : threads) {
				join(thread);
			}
			join(serverThread);
		}
	}

	private static void join(Thread thread) {
		try {
			thread.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

}
