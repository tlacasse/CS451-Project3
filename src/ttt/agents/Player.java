package ttt.agents;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import ttt.Board;
import ttt.Code;
import ttt.learning.GameIO;
import ttt.learning.Matrix;
import ttt.learning.NeuralNetwork;
import ttt.learning.Training;

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
	private final NeuralNetwork nn;

	public Player(int port) throws IOException {
		super(port);
		board = new Board(Board.Type.PLAYER);
		nn = pickRandomNN();
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
			case Code.FULL_BOARD:
				print("Full Board!");
				return false;
			default:
				throw new UnsupportedOperationException("Code: " + mode);
			}
		}
	}

	public int[] choose() {
		// row matrix
		final Matrix out = nn.calculate(new Matrix(false, board.asDoubleArray()));
		final double min = (double) Integer.MIN_VALUE;

		int[] pos = null;
		double max = min;
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

	public void setOtherPlayerMove(int x, int y) {
		board.set(x, y, OTHER);
	}

	private NeuralNetwork pickRandomNN() throws IOException {
		return GameIO.loadNetwork(Training.NETWORKS[RANDOM.nextInt(Training.NETWORK_COUNT)]);
	}

	// hash code seems to be the same, so recreate output
	// won't be able to tell "this is player x", but can tell players apart.
	private final String strOut = "Player@" + UUID.randomUUID().toString().substring(0, 8) + ": ";

	private void print(String s) {
		System.out.println(strOut + s);
	}

}
