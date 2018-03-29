package ttt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ttt.agents.Server;
import ttt.agents.Spawn;
import ttt.learning.GameIO;

public class Game {

	private static final int MOVE_SIZE = (Integer.BYTES * 3);

	private List<Integer> moves;
	private int winner;
	private int count;

	private Game() {
		moves = new LinkedList<>();
		count = 0;
		winner = -1;
	}

	public void recordMove(int player, int x, int y) {
		moves.add(player);
		moves.add(x);
		moves.add(y);
		count++;
	}

	public void setWinner(int winner) {
		this.winner = winner;
	}

	public ByteBuffer toByteBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate((Integer.BYTES * 2) + (MOVE_SIZE * count));
		buffer.putInt(winner);
		buffer.putInt(count);
		for (Integer i : moves) {
			buffer.putInt(i.intValue());
		}
		return buffer;
	}

	public static void start(int port, Config config) throws IOException {
		Game game = new Game();
		try (Server server = new Server(game, port, config)) {
			final ArrayList<Thread> threads = new ArrayList<>();
			final Thread serverThread = new Thread(server);

			for (int i = 0; i < config.get(Param.PLAYERS); i++) {
				threads.add(new Thread(new Spawn(port)));
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
		GameIO.saveGame(game.winner == -1 ? null : game);
	}

	private static void join(Thread thread) {
		try {
			thread.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

}
