package ttt;

import static ttt.Program.HAVE_USER;
import static ttt.Program.PLAYERS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ttt.agents.Server;
import ttt.agents.Spawn;
import ttt.learning.GameIO;
import ttt.learning.GamePostfix;

public class Game {

	private static final int MOVE_SIZE = (Integer.BYTES * 3);

	private List<Integer> moves;
	private int winner;
	private int count;
	private int players;

	public Game(int players) {
		this.players = players;
		moves = new LinkedList<>();
		count = 0;
		winner = -1;
	}

	public void setPlayerCount(int players) {
		this.players = players;
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

	public boolean hasWinner() {
		return winner != -1;
	}

	public ByteBuffer toByteBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate((Integer.BYTES * 3) + (MOVE_SIZE * count));
		buffer.putInt(players);
		buffer.putInt(winner);
		buffer.putInt(count);
		for (Integer i : moves) {
			buffer.putInt(i.intValue());
		}
		return buffer;
	}

	public static void start(int port, Config config) throws IOException {
		final Game game = new Game(config.get(PLAYERS));
		try (Server server = new Server(game, port, config)) {
			final ArrayList<Thread> threads = new ArrayList<>();
			final Thread serverThread = new Thread(server);
			final int spawnCount = config.get(PLAYERS) - (config.get(HAVE_USER));

			for (int i = 0; i < spawnCount; i++) {
				threads.add(Spawn.newPlayer(port));
			}

			serverThread.start();
			for (Thread thread : threads) {
				thread.start();
			}
			// run game
			for (Thread thread : threads) {
				Program.join(thread);
			}
			Program.join(serverThread);
		}
		GameIO.saveGame(game, config.get(HAVE_USER) > 0 ? GamePostfix.USER_VS_AI : GamePostfix.NONE);
	}

}
