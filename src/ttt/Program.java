package ttt;

import static ttt.util.TTTUtil.isYes;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import ttt.util.TTTUtil;

/**
 * Main method, can run Tic Tac Toe games and train the Neural Networks.
 */
public final class Program {

	public static final int PORT = 6327;

	public static void main(String[] args) throws IOException {
		try (Scanner scan = new Scanner(System.in)) {
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

	//////////////////////////////////////////////////////////////////////////////////////////

	public static final Config.Key PLAYERS = new Config.Key("Players", 2);
	public static final Config.Key HAVE_USER = new Config.Key("Have a User Input player", 0, true);

	private static final List<Config.Key> KEYS_GAME;

	static {
		KEYS_GAME = TTTUtil.defineList(PLAYERS, HAVE_USER);
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * try (PrintWriter writer = new PrintWriter("Output.txt", "UTF-8")) {
	 * System.out.println("1"); Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>>
	 * s = GameIO .readGamesForNetworkTraining(Collections.emptyList());
	 * System.out.println("2"); s.getKey().getKey().toFile(writer); //
	 * s.getKey().getValue().toFile(writer); //
	 * s.getValue().getKey().toFile(writer); //
	 * s.getValue().getValue().toFile(writer); } System.exit(0);
	 */

	private Program() {
	}

}
