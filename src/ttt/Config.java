package ttt;

import java.util.HashMap;
import java.util.Scanner;

public class Config {

	private static enum Key {
		PLAYERS("Players", 2), HAVEUSER("Have a User Input player", 0, true);

		public final String desc;
		public final short base;
		public final boolean allowZero;

		private Key(String desc, int base, boolean allowZero) {
			this.desc = desc;
			this.base = (short) base;
			this.allowZero = allowZero;
		}

		private Key(String desc, int base) {
			this(desc, base, false);
		}
	}

	private final HashMap<Key, Integer> values;
	private final Scanner scan;

	private Config(Scanner scan) {
		this.scan = scan;
		values = new HashMap<>();
	}

	public static Config create(Scanner scan) {
		final Config config = new Config(scan);
		System.out.println("\n\n=== Change Parameters, leave blank for default ===");
		for (Key key : Key.values()) {
			System.out.println(key.desc + " (" + key.base + "):");
			final String line;
			final int value = (line = scan.nextLine()).equals("") ? key.base : Integer.parseInt(line);
			if (value < (key.allowZero ? 0 : 1)) {
				throw new IllegalArgumentException(key.desc + " must be greater than 0.");
			}
			config.values.put(key, value);
		}
		return config;
	}

	public Scanner getScanner() {
		return scan;
	}

	public int get(Param param) {
		if (param == Param.PLAYERS) {
			return values.get(Key.PLAYERS).intValue();
		}
		if (param == Param.HAVE_USER) {
			final int val = values.get(Key.HAVEUSER).intValue();
			return val > 0 ? 1 : 0;
		}
		return -1;
	}

}
