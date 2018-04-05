package ttt.agents;

import java.io.IOException;
import java.util.Queue;

import ttt.Board;
import ttt.Code;
import ttt.Game;

public abstract class ServerLoop {

	protected <Client extends SocketReadWrite> void processClient(Game game, Board board, Queue<Client> clients,
			int turn) throws IOException, EndGameException {
		System.out.println("Turn: " + turn);
		final Client active = clients.poll();
		active.writeByte(Code.TURN);
		active.flush();
		final byte mode = active.readByte();
		switch (mode) {
		case Code.MOVE:
			final int x = active.readInt();
			final int y = active.readInt();
			board.set(x, y, turn);
			game.recordMove(turn, x, y);
			System.out.println("Move: " + x + ", " + y);
			for (Client other : clients) {
				other.writeByte(Code.OTHER_PLAYER_MOVE);
				other.writeInt(x);
				other.writeInt(y);
				other.flush();
			}
			final boolean isWin = board.isWin(turn, x, y);
			if (isWin || board.isFull()) {
				final byte code = isWin ? Code.GAME_DONE : Code.FULL_BOARD;
				clients.offer(active);
				for (Client client : clients) {
					client.writeByte(code);
					client.flush();
				}
				if (isWin) {
					game.setWinner(turn);
					System.out.println("Winner: " + turn);
				}
				throw new EndGameException();
			}
			break;
		default:
			throw new UnsupportedOperationException("Code: " + mode);
		}
		clients.offer(active);
	}

	protected static final class EndGameException extends Exception {
		private static final long serialVersionUID = 3082137597765604600L;
	}

}
