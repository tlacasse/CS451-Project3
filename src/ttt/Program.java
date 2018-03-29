package ttt;

import java.io.IOException;
import java.util.Scanner;

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
					config = Config.create(scan);
				}
				Game.start(PORT, config);
				System.out.println("??? Go Again? (y/n)");
				line = scan.nextLine();
			}
		}
	}

	private static boolean isYes(String line) {
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

	private Program() {
	}

}
