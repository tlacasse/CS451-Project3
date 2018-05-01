package ttt;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import ttt.learning.GamePostfix;
import ttt.learning.Training;

/**
 * Main method, can run Tic Tac Toe games and train the Neural Networks.
 */
public final class Program {

	public static final int PORT = 6327;

	public static void main(String[] args) throws IOException {
		/*
		 * try (PrintWriter writer = new PrintWriter("Output.txt", "UTF-8")) {
		 * System.out.println("1"); Pair<Pair<Matrix, Matrix>, Pair<Matrix,
		 * Matrix>> s = GameIO
		 * .readGamesForNetworkTraining(Collections.emptyList());
		 * System.out.println("2"); s.getKey().getKey().toFile(writer); //
		 * s.getKey().getValue().toFile(writer); //
		 * s.getValue().getKey().toFile(writer); //
		 * s.getValue().getValue().toFile(writer); } System.exit(0);
		 */
		try (Scanner scan = new Scanner(System.in)) {
			System.out.println("??? Train Neural Networks? (y/n)");
			if (isYes(scan.nextLine())) {
				train(scan);
			}
			System.out.println("??? Run Tic Tac Toe? (y/n)");
			if (isYes(scan.nextLine())) {
				String line = "y";
				Config config = null;
				while (isYes(line)) {
					if (config != null) {
						System.out.println("??? Use Same Parameters? (y/n)");
						config = isYes(scan.nextLine()) ? config : null;
					}
					if (config == null) {
						config = Config.create(scan, KEYS_GAME);
					}
					Game.start(PORT, config);
					System.out.println("??? Go Again? (y/n)");
					line = scan.nextLine();
				}
			}
		}
	}

	private static void train(Scanner scan) throws IOException {
		System.out.println("??? Start from Random Weights? (y/n)");
		if (isYes(scan.nextLine())) {
			Training.restartNeuralNetworks();
		}
		Config config = Config.create(scan, KEYS_TRAINING);

		List<GamePostfix> gamesToExclude = new LinkedList<>();
		if (config.get(TRAINING_USE_AvA) < 1)
			gamesToExclude.add(GamePostfix.NONE);
		if (config.get(TRAINING_USE_PvA) < 1)
			gamesToExclude.add(GamePostfix.USER_VS_AI);
		if (config.get(TRAINING_USE_PvP) < 1)
			gamesToExclude.add(GamePostfix.PVP);

		Training.train(config.get(TRAINING_ITERATIONS), config.get(TRAINING_DISPLAY_INTERVALS),
				config.get(TRAINING_USE_LOSS) > 0, gamesToExclude);
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	public static final Config.Key PLAYERS = new Config.Key("Players", 2);
	public static final Config.Key HAVE_USER = new Config.Key("Have a User Input player", 0, true);

	public static final Config.Key TRAINING_ITERATIONS = new Config.Key("Training Iterations", 500);
	public static final Config.Key TRAINING_DISPLAY_INTERVALS = new Config.Key(
			"Number of Display Intervals while Training", 10);
	public static final Config.Key TRAINING_USE_LOSS = new Config.Key("Use Losses to 'unlearn'", 1, true);
	public static final Config.Key TRAINING_USE_AvA = new Config.Key("Use AI vs AI games", 1, true);
	public static final Config.Key TRAINING_USE_PvA = new Config.Key("Use Player vs AI games", 1, true);
	public static final Config.Key TRAINING_USE_PvP = new Config.Key("Use Player vs Player games", 1, true);

	private static final List<Config.Key> KEYS_GAME, KEYS_TRAINING;

	static {
		KEYS_GAME = defineList(PLAYERS, HAVE_USER);
		KEYS_TRAINING = defineList(TRAINING_ITERATIONS, TRAINING_DISPLAY_INTERVALS, TRAINING_USE_LOSS, TRAINING_USE_AvA,
				TRAINING_USE_PvA, TRAINING_USE_PvP);
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	public static boolean isYes(String line) {
		if (line == null) {
			return false;
		}
		if (line.equals("")) {
			return false;
		}
		line = line.toLowerCase();
		if (line.charAt(0) == 'y') {
			return true;
		}
		return false;
	}

	@SafeVarargs
	public static <T> List<T> defineList(T... ts) {
		if (ts.length == 0) {
			throw new IllegalArgumentException("Need at least one element.");
		}
		LinkedList<T> list = new LinkedList<>();
		for (int i = 0; i < ts.length; i++) {
			list.add(ts[i]);
		}
		return list;
	}

	public static void join(Thread thread) {
		try {
			thread.join();
		} catch (InterruptedException ie) {
			System.out.println("!!!!! Failed to join: " + thread);
			ie.printStackTrace();
		}
	}

	public static int coordToOrdinal(int x, int y, int size) {
		return x + (y * size);
	}

	public static int[] ordinalToCoord(int n, int size) {
		return new int[] { n % size, Math.floorDiv(n, size) };
	}

	public static int[] strArrayToIntArray(String... strs) {
		final int[] result = new int[strs.length];
		for (int i = 0; i < strs.length; i++) {
			result[i] = Integer.parseInt(strs[i]);
		}
		return result;
	}

	private Program() {
	}

}
