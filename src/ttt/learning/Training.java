package ttt.learning;

import java.io.IOException;
import java.util.LinkedList;
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

	public static void restartNeuralNetworks() throws IOException {
		for (int i = 0; i < NETWORKS.length; i++) {
			GameIO.saveNetwork(new NeuralNetwork(NETWORKS[i]));
		}
	}

	private static Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> DATA;

	public static void train(int interations, int displayIntervals) throws IOException {
		DATA = GameIO.readGamesForNetworkTraining();
		final LinkedList<NeuralNetwork> networks = new LinkedList<>();
		for (int i = 0; i < NETWORKS.length; i++) {
			networks.add(GameIO.loadNetwork(NETWORKS[i]));
		}
		final int displayInterval = interations / displayIntervals;
		for (int t = 1; t <= interations; t++) {
			if (t % displayInterval == 0 || t == interations) {
				System.out.println("Cost at " + t + ":");
				for (NeuralNetwork nn : networks) {
					nn.calculate(winData().getKey());
					System.out.println("W - " + nn + ":\t\t" + nn.cost(winData().getValue()));
					nn.calculate(lossData().getKey());
					System.out.println("L - " + nn + ":\t\t" + nn.cost(lossData().getValue()));
				}
			}
			for (NeuralNetwork nn : networks) {
				Matrix[] derivative, weights;
				// reference -> no need to set again
				weights = nn.getWeights();

				nn.calculate(winData().getKey());
				derivative = nn.costPrime(winData().getValue());
				for (int w = 0; w < weights.length; w++) {
					// win data -> move downhill -> negative derivative
					weights[w] = weights[w].add(derivative[w].negative());
				}

				nn.calculate(lossData().getKey());
				derivative = nn.costPrime(lossData().getValue());
				for (int w = 0; w < weights.length; w++) {
					// loss data -> move uphill -> positive derivative
					weights[w] = weights[w].add(derivative[w]);
				}
			}
		}
		for (NeuralNetwork nn : networks) {
			GameIO.saveNetwork(nn);
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
