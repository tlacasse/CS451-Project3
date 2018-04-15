package ttt;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public final class Config {

	public static final class Key {

		public final String desc;
		public final int base;
		public final boolean isBoolean;

		public Key(String desc, int base, boolean isBoolean) {
			this.desc = desc;
			this.base = base;
			this.isBoolean = isBoolean;
		}

		public Key(String desc, int base) {
			this(desc, base, false);
		}

	}

	private final HashMap<Key, Integer> values;
	private final Scanner scan;

	private Config(Scanner scan) {
		this.scan = scan;
		values = new HashMap<>();
	}

	public static Config create(Scanner scan, List<Key> keys) {
		final Config config = new Config(scan);
		System.out.println("\n\n=== Change Parameters, leave blank for default ===");
		for (Key key : keys) {
			System.out.println(key.desc + " (" + key.base + "):");
			final String line;
			final int value = (line = scan.nextLine()).equals("") ? key.base : Integer.parseInt(line);
			if (!key.isBoolean && value < 1) {
				throw new IllegalArgumentException(key.desc + " must be greater than 0.");
			}
			config.values.put(key, value);
		}
		return config;
	}

	public int get(Key key) {
		final int result = values.get(key).intValue();
		return key.isBoolean ? (result > 0 ? 1 : 0) : result;
	}

	public Scanner getScanner() {
		return scan;
	}

}
