package ttt.learning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javafx.util.Pair;
import ttt.Board;
import ttt.Config;
import ttt.util.GameIO;
import ttt.util.GamePostfix;
import ttt.util.TTTUtil;

/**
 * Static method to train the networks, along with storing the networks to use.
 */
public class Training {

	public static void main(String[] args) throws IOException {
		try (Scanner scan = new Scanner(System.in)) {
			System.out.println("??? Start from Random Weights? (y/n)");
			if (TTTUtil.isYes(scan.nextLine())) {
				Training.restartNeuralNetworks();
			}
			final Config config = Config.create(scan, KEYS_TRAINING);

			List<GamePostfix> gamesToExclude = new LinkedList<>();
			if (config.get(TRAINING_USE_AvA) < 1)
				gamesToExclude.add(GamePostfix.NONE);
			if (config.get(TRAINING_USE_PvA) < 1)
				gamesToExclude.add(GamePostfix.USER_VS_AI);
			if (config.get(TRAINING_USE_PvP) < 1)
				gamesToExclude.add(GamePostfix.PVP);

			train(config.get(TRAINING_ITERATIONS), config.get(TRAINING_DISPLAY_INTERVALS),
					config.get(TRAINING_USE_LOSS) > 0, gamesToExclude);
		}
	}

	public static final Config.Key TRAINING_ITERATIONS = new Config.Key("Training Iterations", 500);
	public static final Config.Key TRAINING_DISPLAY_INTERVALS = new Config.Key(
			"Number of Display Intervals while Training", 10);
	public static final Config.Key TRAINING_USE_LOSS = new Config.Key("Use Losses to 'unlearn'", 1, true);
	public static final Config.Key TRAINING_USE_AvA = new Config.Key("Use AI vs AI games", 1, true);
	public static final Config.Key TRAINING_USE_PvA = new Config.Key("Use Player vs AI games", 1, true);
	public static final Config.Key TRAINING_USE_PvP = new Config.Key("Use Player vs Player games", 1, true);

	private static final List<Config.Key> KEYS_TRAINING;

	static {
		KEYS_TRAINING = TTTUtil.defineList(TRAINING_ITERATIONS, TRAINING_DISPLAY_INTERVALS, TRAINING_USE_LOSS,
				TRAINING_USE_AvA, TRAINING_USE_PvA, TRAINING_USE_PvP);
	}

	//////////////////////////////////////////////////////////////////////////////////////////

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
		// CNN_NETWORKS[i++] = new int[][] { new int[] { 3, 3 }, new int[] { 50,
		// Board.CELLS } };
		// CNN_NETWORKS[i++] = new int[][] { new int[] { 3, 3, 3 }, new int[] {
		// 13, Board.CELLS } };
		// CNN_NETWORKS[i++] = new int[][] { new int[] { 5, 5 }, new int[] { 13,
		// 26, Board.CELLS } };

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

	private static void restartNeuralNetworks() throws IOException {
		for (int i = 0; i < NN_NETWORKS.length; i++) {
			GameIO.saveNetwork(new NeuralNetwork(NN_NETWORKS[i]));
		}
		for (int i = 0; i < CNN_NETWORKS.length; i++) {
			GameIO.saveNetwork(new Convolutional(Board.SIZE, CNN_NETWORKS[i][0], CNN_NETWORKS[i][1]));
		}
	}

	private static List<AI> loadNetworks() throws FileNotFoundException, IOException {
		final List<AI> list = new LinkedList<>();
		for (int i = 0; i < NN_NETWORKS.length; i++) {
			// list.add(GameIO.loadNetwork(NeuralNetwork.fileName(NN_NETWORKS[i]),
			// false));
		}
		for (int i = 0; i < CNN_NETWORKS.length; i++) {
			list.add(GameIO.loadNetwork(Convolutional.fileName(Board.SIZE, CNN_NETWORKS[i][0], CNN_NETWORKS[i][1])));
		}
		return list;
	}

	private static Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> DATA;

	private static void train(int interations, int displayIntervals, boolean useLosses,
			List<GamePostfix> gamesToExclude) throws IOException {
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
