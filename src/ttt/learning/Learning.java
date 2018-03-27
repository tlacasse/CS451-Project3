package ttt.learning;

import java.util.Random;

import ttt.Board;

public class Learning {

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

	public static NeuralNetwork pickRandomNN() {
		return new NeuralNetwork(NETWORKS[RANDOM.nextInt(8)]);
	}

}
