package ttt;

import static ttt.Program.HAVE_USER;
import static ttt.Program.PLAYERS;
import static ttt.util.TTTUtil.checkRange;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ttt.agents.Server;
import ttt.agents.Spawn;
import ttt.util.GameIO;
import ttt.util.GamePostfix;
import ttt.util.TTTUtil;

/**
 * Represents a Tic Tac Toe game in progress, by storing each move along with
 * player count and winner.
 */
public class Game {

	private static final int MOVE_SIZE = 3;

	private List<Byte> moves;
	private byte winner;
	private byte players;
	private short count;

	private Game(byte players, boolean notUsed) {
		this.players = players;
		moves = new LinkedList<>();
		count = 0;
		winner = -1;
	}

	public Game(int players) {
		this(checkRange(players), true);
	}

	public Game() {
		this((byte) -1, true);
	}

	public void setPlayerCount(int players) {
		this.players = checkRange(players);
	}

	public void setWinner(int winner) {
		this.winner = checkRange(winner);
	}

	public void recordMove(int player, int x, int y) {
		moves.add(checkRange(player));
		moves.add(checkRange(x));
		moves.add(checkRange(y));
		count++;
	}

	public boolean hasWinner() {
		return winner != -1;
	}

	/**
	 * <ul>
	 * <li>Player Count</li>
	 * <li>Winner</li>
	 * <li>Move Count</li>
	 * <li>Moves:
	 * <ul>
	 * <li>Player</li>
	 * <li>X</li>
	 * <li>Y</li>
	 * </ul>
	 * </li>
	 * </ul>
	 */
	public ByteBuffer toByteBuffer() {
		if (players == -1) {
			throw new IllegalStateException("Player Count Not Set.");
		}
		final ByteBuffer buffer = ByteBuffer.allocate(4 + (MOVE_SIZE * count));
		buffer.put(players);
		buffer.put(winner);
		buffer.putShort(count); // short because 13^2 > 127
		for (Byte b : moves) {
			buffer.put(b);
		}
		return buffer;
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	// create the game for the assignment, (not for the website games)
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
				TTTUtil.join(thread);
			}
			TTTUtil.join(serverThread);
		}
		GameIO.saveGame(game, config.get(HAVE_USER) > 0 ? GamePostfix.USER_VS_AI : GamePostfix.NONE);
	}

}
