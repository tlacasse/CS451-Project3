package ttt.agents;

import java.io.IOException;
import java.net.Socket;

import ttt.Board;
import ttt.learning.Learning;
import ttt.learning.Matrix;
import ttt.learning.NeuralNetwork;

public class Player extends SocketSide {

	public static int main(String[] args) throws Exception {
		final int port = Integer.parseInt(args[0]);
		try (Player player = new Player(port)) {

		} catch (Exception e) {
			System.out.println("Failed!");
			e.printStackTrace();
		}
		return 0;
	}

	private static final String LOCALHOST = "127.0.0.1";

	private final Board board;
	private final NeuralNetwork nn;

	public Player(int port) throws IOException {
		super(port, 2 * 4);
		board = new Board();
		nn = Learning.pickRandomNN();
	}

	@Override
	protected void connect(int port) throws IOException {
		if (socket == null || socket.isClosed()) {
			socket = new Socket(LOCALHOST, port);
		}
	}

	public boolean choose() {
		final Matrix out = nn.calculate(board.getBoard());
		final int ord = out.indexOfMax()[1]; // column of row matrix
		final int[] coord = Board.ordinalToCoord(ord);
		return board.set(coord[0], coord[1], Board.SELF);
	}

}
