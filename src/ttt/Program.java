package ttt;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import ttt.learning.Training;

public final class Program {

	public static final int PORT = 6327;

	public static void main(String[] args) throws IOException {
		try (Scanner scan = new Scanner(System.in)) {
			System.out.println("??? Train Neural Networks? (y/n)");
			if (isYes(scan.nextLine())) {
				System.out.println("??? Start from Random Weights? (y/n)");
				if (isYes(scan.nextLine())) {
					Training.restartNeuralNetworks();
				}
				final Config config = Config.create(scan, KEYS_TRAINING);
				Training.train(config.get(TRAINING_ITERATIONS), config.get(TRAINING_DISPLAY_INTERVALS),
						config.get(TRAINING_USE_LOSS) > 0);
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

	public static final Config.Key PLAYERS = new Config.Key("Players", 2);
	public static final Config.Key HAVE_USER = new Config.Key("Have a User Input player", 0, true);
	public static final Config.Key TRAINING_ITERATIONS = new Config.Key("Training Iterations", 500);
	public static final Config.Key TRAINING_DISPLAY_INTERVALS = new Config.Key(
			"Number of Display Intervals while Training", 10);
	public static final Config.Key TRAINING_USE_LOSS = new Config.Key("Use Losses to 'unlearn'", 1, true);

	private static final List<Config.Key> KEYS_GAME, KEYS_TRAINING;

	static {
		KEYS_GAME = defineList(PLAYERS, HAVE_USER);
		KEYS_TRAINING = defineList(TRAINING_ITERATIONS, TRAINING_DISPLAY_INTERVALS, TRAINING_USE_LOSS);
	}

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
		final LinkedList<T> list = new LinkedList<>();
		for (int i = 0; i < ts.length; i++) {
			list.add(ts[i]);
		}
		return list;
	}

	public static void join(Thread thread) {
		try {
			thread.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	private Program() {
	}

}
