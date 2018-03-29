package ttt;

import ttt.learning.Matrix;

public class Board {

	public static final int EMPTY = 0;

	public static final int SIZE = 13;
	public static final int CELLS = SIZE * SIZE;

	private final int[][] board;
	private final double[] matrix;
	private final boolean isServer;
	private int moves;

	// server will add one to player, because 0 means empty;
	public Board(boolean isServerImplementation) {
		board = new int[SIZE][SIZE];
		matrix = new double[CELLS];
		moves = 0;
		isServer = isServerImplementation;
	}

	public Matrix getBoard() {
		// row matrix
		return new Matrix(false, matrix);
	}

	public boolean isFull() {
		return moves == CELLS;
	}

	public boolean isSpaceEmpty(int x, int y) {
		return board[x][y] == EMPTY;
	}

	public void set(int x, int y, int val) {
		val = isServer ? val + 1 : val;
		board[x][y] = val;
		matrix[coordToOrdinal(x, y)] = (double) val;
		moves++;
	}

	public static int coordToOrdinal(int x, int y) {
		return x + (y * SIZE);
	}

	public static int[] ordinalToCoord(int n) {
		return new int[] { n % SIZE, Math.floorDiv(n, SIZE) };
	}

	public boolean isWin(int val, int addedX, int addedY) {
		val = isServer ? val + 1 : val;
		boolean[] win = new boolean[] { true, true, true, true };
		for (int i = 0; i < SIZE; i++) {
			// orthogonal
			win[0] = (win[0] && board[addedX][i] == val);
			win[1] = (win[1] && board[i][addedY] == val);
			// diagonal
			win[2] = (win[2] && board[i][i] == val);
			win[3] = (win[3] && board[i][SIZE - i - 1] == val);
		}
		return win[0] || win[1] || win[2] || win[3];
	}

	@Override
	public String toString() {
		String result = "";
		int maxSize = 0;
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				maxSize = Math.max(maxSize, ("" + board[i][j]).length());
			}
		}
		maxSize += 2;
		for (int i = 0; i < SIZE; i++) {
			result += "[ ";
			for (int j = 0; j < SIZE; j++) {
				int thisSize = ("" + board[i][j]).length();
				int sb = (int) Math.floor((maxSize - thisSize) / 2);
				int sa = maxSize - thisSize - sb;
				for (int k = 0; k < sb; k++) {
					result += " ";
				}
				result += board[i][j];
				for (int k = 0; k < sa; k++) {
					result += " ";
				}
			}
			result += " ]\n";
		}
		return result;
	}

}
