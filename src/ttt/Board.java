package ttt;

import ttt.learning.Matrix;

public class Board {

	public static final int EMPTY = 0;

	public static final int SIZE = 13;
	public static final int CELLS = SIZE * SIZE;

	private final int[][] board;
	private final double[] matrix;

	public Board() {
		board = new int[SIZE][SIZE];
		matrix = new double[CELLS];
	}

	public Matrix getBoard() {
		// row matrix
		return new Matrix(false, matrix);
	}

	public void set(int x, int y, int val) {
		board[x][y] = val;
		matrix[coordToOrdinal(x, y)] = (double) val;
	}

	public static int coordToOrdinal(int x, int y) {
		return x + (y * SIZE);
	}

	public static int[] ordinalToCoord(int n) {
		return new int[] { n % SIZE, Math.floorDiv(n, SIZE) };
	}

	public boolean isWin(int val, int addedX, int addedY) {
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

}
