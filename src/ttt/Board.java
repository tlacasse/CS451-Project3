package ttt;

import java.util.Random;

public class Board {

	private final int[][] board;

	public Board() {
		board = new int[13][13];
	}

	public boolean set(int x, int y, int val) {
		// random end to test
		Random r = new Random();
		return r.nextBoolean() && r.nextBoolean();
	}

}
