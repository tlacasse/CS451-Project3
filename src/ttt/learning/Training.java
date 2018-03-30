package ttt.learning;

import java.io.IOException;
import java.util.Random;

import javafx.util.Pair;
import ttt.Board;

public class Training {

	public static final int[][] NETWORKS;

	static {
		int i = 0;
		NETWORKS = new int[8][];
		NETWORKS[i++] = new int[] { Board.CELLS, 10, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 50, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 100, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 50, 10, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 25, 25, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 25, 25, 25, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 70, 70, 70, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 10, 10, 10, 10, 10, Board.CELLS };
	}

	private static final Random RANDOM = new Random();

	public static NeuralNetwork pickRandomNN() throws IOException {
		return GameIO.loadNetwork(NETWORKS[RANDOM.nextInt(8)]);
	}

	private final Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> DATA;

	public Training() throws IOException {
		DATA = GameIO.readGamesForNetworkTraining();
	}

	public void train() {

	}

	// wrap because it makes more sense than <code>.getKey()</code>
	private Pair<Matrix, Matrix> winData() {
		return DATA.getKey();
	}

	// wrap because it makes more sense than <code>.getValue()</code>
	private Pair<Matrix, Matrix> lossData() {
		return DATA.getValue();
	}

}
