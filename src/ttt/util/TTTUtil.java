package ttt.util;

import java.util.LinkedList;
import java.util.List;

public final class TTTUtil {

	private TTTUtil() {
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

}
