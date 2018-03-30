package ttt.agents;

import java.io.IOException;
import java.net.Socket;

import ttt.Board;
import ttt.Code;
import ttt.learning.Learning;
import ttt.learning.Matrix;
import ttt.learning.NeuralNetwork;
import ttt.learning.Training;

public class Player extends SocketSide {

	public static void main(String[] args) throws IOException, Exception {
		final int port = Integer.parseInt(args[0]);
		try (Player player = new Player(port)) {
			for (;;) {
				final byte mode = player.readByte();
				switch (mode) {
				case Code.TURN:
					final int[] move = player.choose();
					player.writeByte(Code.MOVE);
					player.writeInt(move[0]);
					player.writeInt(move[1]);
					player.flush();
					System.out.println("Turn: " + move[0] + "," + move[1]);
					break;
				case Code.OTHER_PLAYER_MOVE:
					final int x = player.readInt();
					final int y = player.readInt();
					player.setOtherPlayerMove(x, y);
					System.out.println("Other Move: " + x + "," + y);
					break;
				case Code.GAME_DONE:
					System.out.println("Game Done!");
					System.exit(0);
					return;
				case Code.FULL_BOARD:
					System.out.println("Full Board!");
					System.exit(1);
					return;
				default:
					throw new UnsupportedOperationException("Code: " + mode);
				}
			}
		}
	}

	public static final int SELF = 1;
	public static final int OTHER = -1;

	private static final String LOCALHOST = "127.0.0.1";

	private final Board board;
	private final NeuralNetwork nn;

	public Player(int port) throws IOException {
		super(port);
		board = new Board(false);
		nn = Training.pickRandomNN();
	}

	@Override
	protected void connect(int port) throws IOException {
		if (socket == null || socket.isClosed()) {
			socket = new Socket(LOCALHOST, port);
		}
	}

	public int[] choose() {
		// row matrix
		final Matrix out = nn.calculate(new Matrix(false, board.getDoubleArray()));
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

}
