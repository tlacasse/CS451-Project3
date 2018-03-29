package ttt;

import java.util.HashMap;
import java.util.Scanner;

public class Config {

	private static enum Key {
		PLAYERS("Players", 2);

		public final String desc;
		public final short base;

		private Key(String desc, int base) {
			this.desc = desc;
			this.base = (short) base;
		}
	}

	private final HashMap<Key, Integer> values;

	private Config() {
		values = new HashMap<>();
	}

	public static Config create(Scanner scan) {
		final Config config = new Config();
		System.out.println("\n\n=== Change Parameters, leave blank for default ===");
		for (Key key : Key.values()) {
			System.out.println(key.desc + " (" + key.base + "):");
			final String line;
			final int value = (line = scan.nextLine()).equals("") ? key.base : Integer.parseInt(line);
			if (value < 1) {
				throw new IllegalArgumentException(key.desc + " must be greater than 0.");
			}
			config.values.put(key, value);
		}
		return config;
	}

	public int get(Param param) {
		if (param == Param.PLAYERS) {
			return values.get(Key.PLAYERS).intValue();
		}
		return -1;
	}

}
