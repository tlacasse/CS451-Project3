package ttt;

import ttt.learning.Matrix;

public class Board {

	// server sees board as X & O

	// each player sees board as their selections as SELF, and the other
	// player's as OTHER

	public static final byte EMPTY = -1;

	public static final int SIZE = 13;
	public static final int CELLS = SIZE * SIZE;

	private final byte[][] board;
	private final double[] matrix;

	public Board() {
		board = new byte[SIZE][SIZE];
		matrix = new double[CELLS];
	}

	public Matrix getBoard() {
		// row matrix
		return new Matrix(false, matrix);
	}

	public void set(int x, int y, byte val) {
		board[x][y] = val;
		matrix[coordToOrdinal(x, y)] = d(val);
	}

	public static int coordToOrdinal(int x, int y) {
		return x + (y * SIZE);
	}

	public static int[] ordinalToCoord(int n) {
		return new int[] { n % SIZE, Math.floorDiv(n, SIZE) };
	}

	public boolean isWin(byte val, int addedX, int addedY) {
		boolean win1 = true;
		boolean win2 = true;

		// orthogonal
		for (int i = 0; i < SIZE; i++) {
			if (win1 = win1 && board[addedX][i] == val) {
				return true;
			}
			if (win2 = win2 && board[i][addedY] == val) {
				return true;
			}
		}

		win1 = (win2 = true);
		// diagonals
		for (int i = 0; i < SIZE; i++) {
			if (win1 = win1 && board[i][i] == val) {
				return true;
			}
			if (win2 = win2 && board[i][SIZE - i - 1] == val) {
				return true;
			}
		}

		return false;
	}

	private static double d(byte b) {
		return (double) b;
	}
}
