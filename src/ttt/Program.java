package ttt;

import java.io.IOException;
import java.util.ArrayList;

import ttt.agents.Server;
import ttt.agents.Spawn;

public class Program {

	public static final int PORT = 6327;

	public static void main(String[] args) throws IOException, Exception {
	}

	private static void go() throws IOException, Exception {
		try (Server server = new Server(PORT)) {
			final ArrayList<Thread> threads = new ArrayList<>();
			final Thread serverThread = new Thread(server);

			threads.add(new Thread(new Spawn(PORT)));
			threads.add(new Thread(new Spawn(PORT)));

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
