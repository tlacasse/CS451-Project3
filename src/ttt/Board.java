package ttt;

import ttt.learning.Matrix;

public class Board {

	public static final byte X = 1;
	public static final byte O = -1;
	public static final byte EMPTY = 0;

	public static final int SIZE = 13;
	public static final int CELLS = SIZE * SIZE;

	private final Matrix board;

	public Board() {
		board = new Matrix(SIZE, SIZE, d(EMPTY));
	}

	public Matrix getBoard() {
		return board;
	}

	public boolean set(int x, int y, byte val) {
		board.set(x, y, d(val));
		return isWin(d(val), x, y);
	}

	private boolean isWin(double val, int addedX, int addedY) {
		boolean win1 = true;
		boolean win2 = true;

		// orthogonal
		for (int i = 0; i < SIZE; i++) {
			if (win1 = win1 && board.get(addedX, i) == val) {
				return true;
			}
			if (win2 = win2 && board.get(i, addedY) == val) {
				return true;
			}
		}

		win1 = (win2 = true);
		// diagonals
		for (int i = 0; i < SIZE; i++) {
			if (win1 = win1 && board.get(i, i) == val) {
				return true;
			}
			if (win2 = win2 && board.get(i, SIZE - i - 1) == val) {
				return true;
			}
		}

		return false;
	}

	private static double d(byte b) {
		return (double) b;
	}

}
