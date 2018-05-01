package ttt.agents;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import ttt.Board;
import ttt.Code;
import ttt.learning.AI;
import ttt.learning.Convolutional;
import ttt.learning.Matrix;
import ttt.learning.NeuralNetwork;
import ttt.learning.Training;
import ttt.util.GameIO;

/**
 * Player process that plays Tic Tac Toe through sockets.
 */
public class Player extends SocketSide implements AutoCloseable {

	public static void main(String[] args) throws IOException {
		final int port = Integer.parseInt(args[0]);
		boolean result = false; // true if game has winner else false is tie
		try (Player player = new Player(port)) {
			result = player.play();
		}
		System.exit(result ? 0 : 1);
	}

	public static final int SELF = 1;
	public static final int OTHER = -1;

	private static final String LOCALHOST = "127.0.0.1";
	private static final Random RANDOM = new Random();

	private final Board board;
	private final AI nn;

	// add a random first movie to vary games
	private boolean first;

	public Player(int port) throws IOException {
		super(port);
		board = new Board(Board.Type.PLAYER);
		nn = pickRandomNN();
		System.out.println("Neural Network Chosen: " + nn);
		first = true;
	}

	@Override
	protected void connect(int port) throws IOException {
		if (socket == null || socket.isClosed()) {
			socket = new Socket(LOCALHOST, port);
		}
	}

	public boolean play() throws IOException {
		for (;;) {
			final byte mode = readByte();
			switch (mode) {
			case Code.TURN:
				final int[] move = choose();
				writeByte(Code.MOVE);
				writeInt(move[0]);
				writeInt(move[1]);
				flush();
				print("Take Turn: " + move[0] + ", " + move[1]);
				break;
			case Code.OTHER_PLAYER_MOVE:
				final int x = readInt();
				final int y = readInt();
				setOtherPlayerMove(x, y);
				print("Other Move: " + x + ", " + y);
				break;
			case Code.GAME_DONE:
				print("Game Done!");
				return true;
			case Code.GAME_TIE:
				print("Game is a Tie!");
				return false;
			case Code.CONNECTED:
				// code only used on website client
				break;
			default:
				throw new UnsupportedOperationException("Code: " + mode);
			}
		}
	}

	private int[] choose() {
		if (first) {
			first = false;
			return new int[] { RANDOM.nextInt(Board.SIZE), RANDOM.nextInt(Board.SIZE) };
		}
		// row matrix
		final Matrix out = nn.calculate(new Matrix(false, board.asDoubleArray()));

		int[] pos = null;
		double max = (double) Integer.MIN_VALUE;
		for (int i = 0; i < Board.CELLS; i++) {
			final int[] coord = Board.ordinalToCoord(i);
			final double get = out.get(0, i);

			if (get > max && board.isSpaceEmpty(coord[0], coord[1])) {
				max = get;
				pos = coord;
			}
		}

		board.set(pos[0], pos[1], SELF);
		return pos;
	}

	private void setOtherPlayerMove(int x, int y) {
		first = false;
		board.set(x, y, OTHER);
	}

	private AI pickRandomNN() throws IOException {
		if (RANDOM.nextBoolean()) {
			return GameIO.loadNetwork(
					NeuralNetwork.fileName(Training.NN_NETWORKS[RANDOM.nextInt(Training.NN_NETWORK_COUNT)]), false);
		} else {
			int[][] get = Training.CNN_NETWORKS[RANDOM.nextInt(Training.CNN_NETWORK_COUNT)];
			return GameIO.loadNetwork(Convolutional.fileName(Board.SIZE, get[0], get[1]), true);
		}
	}

	// hash code seems to be the same, so recreate output
	// won't be able to tell "this is player x", but can tell players apart.
	private final String strOut = "Player@" + UUID.randomUUID().toString().substring(0, 8) + ": ";

	private void print(String s) {
		System.out.println(strOut + s);
	}

}
