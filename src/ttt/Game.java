package ttt;

import java.io.IOException;
import java.util.ArrayList;

import ttt.agents.Server;
import ttt.agents.Spawn;

public final class Game {

	public static void start(int port) throws IOException {
		try (Server server = new Server(port)) {
			final ArrayList<Thread> threads = new ArrayList<>();
			final Thread serverThread = new Thread(server);

			threads.add(new Thread(new Spawn(port)));
			threads.add(new Thread(new Spawn(port)));

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

	private Game() {
	}

}
