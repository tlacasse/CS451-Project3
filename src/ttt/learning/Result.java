package ttt.learning;

import java.io.IOException;
import java.util.List;

import javafx.util.Pair;

public class Result {

	private static List<Result> GAMES = null;

	public static void generate() throws IOException {
		GAMES = GameIO.readGames();
	}

	///////////////////////////////////////////////////////////////////

	public final int players;
	public final int winner;
	public final int count;

	private final int[][] moves;

	private int index;

	public Result(int players, int winner, int count) {
		this.players = players;
		this.winner = winner;
		this.count = count;
		moves = new int[count][3];
		index = 0;
	}

	public void addMove(int player, int x, int y) {
		moves[index++] = new int[] { player, x, y };
	}

	// Pair: key is input, value is output
	public Pair<Matrix, Matrix> generateMatrices(int player) {
		return null;
	}

}
