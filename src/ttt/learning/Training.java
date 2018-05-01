package ttt.learning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;
import ttt.Board;
import ttt.util.GameIO;
import ttt.util.GamePostfix;

/**
 * Static method to train the networks, along with storing the networks to use.
 */
public class Training {

	public static final int[][] NN_NETWORKS;
	public static final int[][][] CNN_NETWORKS;
	public static final int NN_NETWORK_COUNT, CNN_NETWORK_COUNT;

	static {
		int i = 0;
		NN_NETWORKS = new int[NN_NETWORK_COUNT = 4][];

		NN_NETWORKS[i++] = new int[] { Board.CELLS, 50, Board.CELLS };
		NN_NETWORKS[i++] = new int[] { Board.CELLS, 50, 10, Board.CELLS };
		NN_NETWORKS[i++] = new int[] { Board.CELLS, 15, 15, Board.CELLS };
		NN_NETWORKS[i++] = new int[] { Board.CELLS, 8, 8, Board.CELLS };

		i = 0;
		CNN_NETWORKS = new int[CNN_NETWORK_COUNT = 1][][];
		CNN_NETWORKS[i++] = new int[][] { new int[] { 5, 5 }, new int[] { 50, Board.CELLS } };

		// these are not good
		// NETWORKS[i++] = new int[] { Board.CELLS, 5, Board.CELLS };
		// NETWORKS[i++] = new int[] { Board.CELLS, 5, 5, 5, Board.CELLS };
		// NETWORKS[i++] = new int[] { Board.CELLS, 3, 5, 7, Board.CELLS };
		// NETWORKS[i++] = new int[] { Board.CELLS, 10, Board.CELLS };
		// NETWORKS[i++] = new int[] { Board.CELLS, 100, Board.CELLS };
		// NETWORKS[i++] = new int[] { Board.CELLS, 25, 25, Board.CELLS };
		// NETWORKS[i++] = new int[] { Board.CELLS, 25, 25, 25, Board.CELLS };
		// NETWORKS[i++] = new int[] { Board.CELLS, 70, 70, 70, Board.CELLS };
		// NETWORKS[i++] = new int[] { Board.CELLS, 10, 10, 10, 10, 10,
		// Board.CELLS };
	}

	public static void restartNeuralNetworks() throws IOException {
		for (AI nn : createNetworks()) {
			GameIO.saveNetwork(nn);
		}
	}

	private static List<AI> createNetworks() {
		final List<AI> list = new LinkedList<>();
		for (int i = 0; i < NN_NETWORKS.length; i++) {
			list.add(new NeuralNetwork(NN_NETWORKS[i]));
		}
		for (int i = 0; i < CNN_NETWORKS.length; i++) {
			list.add(new Convolutional(Board.SIZE, CNN_NETWORKS[i][0], CNN_NETWORKS[i][1]));
		}
		return list;
	}

	private static List<AI> loadNetworks() throws FileNotFoundException, IOException {
		final List<AI> list = new LinkedList<>();
		for (int i = 0; i < NN_NETWORKS.length; i++) {
			list.add(GameIO.loadNetwork(NeuralNetwork.fileName(NN_NETWORKS[i]), false));
		}
		for (int i = 0; i < CNN_NETWORKS.length; i++) {
			list.add(GameIO.loadNetwork(Convolutional.fileName(Board.SIZE, CNN_NETWORKS[i][0], CNN_NETWORKS[i][1]),
					true));
		}
		return list;
	}

	private static Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> DATA;

	public static void train(int interations, int displayIntervals, boolean useLosses, List<GamePostfix> gamesToExclude)
			throws IOException {
		DATA = GameIO.readGamesForNetworkTraining(gamesToExclude);
		System.out.println("Win Training Data Points: " + winData().getKey().rows());
		System.out.println("Loss Training Data Points: " + lossData().getKey().rows());
		System.out.println();

		final int displayInterval = interations / displayIntervals;

		for (AI nn : loadNetworks()) {
			System.out.println(nn);
			for (int t = 1; t <= interations; t++) {
				if (t % displayInterval == 0 || t == interations) {
					nn.calculate(winData().getKey());
					System.out.println("Cost at " + t + ":\t" + nn.cost(winData().getValue()));
				}

				Matrix[] derivative, weights;
				// reference -> no need to set again
				weights = nn.getWeights();

				nn.calculate(winData().getKey());
				derivative = nn.costPrime(winData().getValue());
				for (int w = 0; w < weights.length; w++) {
					// win data -> move downhill -> negative derivative
					weights[w] = weights[w].add(derivative[w].negative());
				}
				if (useLosses) {
					nn.calculate(lossData().getKey());
					derivative = nn.costPrime(lossData().getValue());
					for (int w = 0; w < weights.length; w++) {
						// loss data -> move uphill -> positive derivative (but
						// don't change as much)
						weights[w] = weights[w].add(derivative[w].scalar(0.5));
					}
				}
			}
			GameIO.saveNetwork(nn);
			System.out.println();
		}
	}

	// wrap because it makes more sense than <code>.getKey()</code>
	private static Pair<Matrix, Matrix> winData() {
		return DATA.getKey();
	}

	// wrap because it makes more sense than <code>.getValue()</code>
	private static Pair<Matrix, Matrix> lossData() {
		return DATA.getValue();
	}

}
