package ttt.learning;

import java.io.IOException;
import java.util.List;

import javafx.util.Pair;
import ttt.Board;

/**
 * Static method to train the networks, along with storing the networks to use.
 */
public class Training {

	public static final int[][] NETWORKS;
	public static final int NETWORK_COUNT;

	static {
		int i = 0;
		NETWORKS = new int[NETWORK_COUNT = 4][];

		NETWORKS[i++] = new int[] { Board.CELLS, 50, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 50, 10, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 15, 15, Board.CELLS };
		NETWORKS[i++] = new int[] { Board.CELLS, 8, 8, Board.CELLS };

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
		for (int i = 0; i < NETWORKS.length; i++) {
			GameIO.saveNetwork(new NeuralNetwork(NETWORKS[i]));
		}
	}

	private static Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> DATA;

	public static void train(int interations, int displayIntervals, boolean useLosses, List<GamePostfix> gamesToExclude)
			throws IOException {
		DATA = GameIO.readGamesForNetworkTraining(gamesToExclude);
		System.out.println("Win Training Data Points: " + winData().getKey().rows());
		System.out.println("Loss Training Data Points: " + lossData().getKey().rows());
		System.out.println();

		final int displayInterval = interations / displayIntervals;

		for (int i = 0; i < NETWORKS.length; i++) {
			NeuralNetwork nn = GameIO.loadNetwork(NETWORKS[i]);
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
