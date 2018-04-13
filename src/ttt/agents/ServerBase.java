package ttt.agents;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;

import ttt.Board;
import ttt.Code;
import ttt.Game;

public abstract class ServerBase implements AutoCloseable {

	protected final ServerSocket server;
	protected final Game game;
	protected final Board board;
	protected final Queue<Client> clients;
	protected int turn;

	public ServerBase(Game game, int port) throws IOException {
		this.game = game;
		board = new Board(Board.Type.SERVER);
		clients = new LinkedList<>();

		server = new ServerSocket(port);
		System.out.println(server);

		turn = 0;
	}

	protected void processClient() throws IOException, EndGameException {
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
			if (isWin || board.isTie()) {
				final byte code = isWin ? Code.GAME_DONE : Code.GAME_TIE;
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

	@Override
	public void close() throws IOException {
		for (Client client : clients) {
			if (client != null) {
				client.close();
			}
		}
		server.close();
	}

	protected interface Client extends SocketReadWrite {
	}

	protected static final class EndGameException extends Exception {
		private static final long serialVersionUID = 3082137597765604600L;
	}

}
