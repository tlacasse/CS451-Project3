package ttt.agents;

import java.io.IOException;
import java.net.Socket;

import ttt.Board;
import ttt.Code;
import ttt.learning.Learning;
import ttt.learning.Matrix;
import ttt.learning.NeuralNetwork;

public class Player extends SocketSide {

	public static final byte SELF = 1;
	public static final byte OTHER = -1;

	public static int main(String[] args) throws IOException {
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
					player.send();
					break;
				case Code.OTHER_PLAYER_MOVE:
					final int x = player.readInt();
					final int y = player.readInt();
					player.setOtherPlayerMove(x, y);
					break;
				case Code.GAME_DONE:
					final boolean didWin = player.readBoolean();
					return didWin ? 1 : 0;
				default:
					throw new UnsupportedOperationException();
				}
			}
		}
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

	public int[] choose() {
		final Matrix out = nn.calculate(board.getBoard());
		final int ord = out.indexOfMax()[1]; // column of row matrix
		final int[] coord = Board.ordinalToCoord(ord);
		board.set(coord[0], coord[1], SELF);
		return coord;
	}

	public void setOtherPlayerMove(int x, int y) {
		board.set(x, y, OTHER);
	}

}
