package ttt;

import java.io.IOException;

import ttt.learning.GameIO;
import ttt.learning.NeuralNetwork;

public final class Program {

	public static final int PORT = 6327;

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

	public static void main(String[] args) throws IOException, Exception {
		for (int[] i : NETWORKS) {
			GameIO.saveNetwork(new NeuralNetwork(i));
		}
	}

	private Program() {
	}

}
