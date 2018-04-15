package ttt;

import java.util.Arrays;

public class Board {

	public static enum Type {
		/**
		 * <ul>
		 * <li>0 = empty</li>
		 * <li>1 = player 0</li>
		 * <li>2 = player 1</li>
		 * </ul>
		 * Also keeps track of ties.
		 */
		SERVER,
		/**
		 * <ul>
		 * <li>0 = empty</li>
		 * <li>1 = player move</li>
		 * <li>-1 = other player move</li>
		 * </ul>
		 * Also stores a double array of board.
		 */
		PLAYER;
	}

	public static final int EMPTY = 0;
	public static final int SIZE = 13;
	public static final int CELLS = SIZE * SIZE;

	private final int[][] board;
	private final Type type;
	private final double[] matrix;
	private final TieTracker tt;

	public Board(Type type) {
		this.type = type;
		board = new int[SIZE][SIZE];
		if (type == Type.PLAYER) {
			matrix = new double[CELLS];
			tt = null;
		} else {
			tt = new TieTracker();
			matrix = null;
		}
	}

	public boolean isSpaceEmpty(int x, int y) {
		return board[x][y] == EMPTY;
	}

	public void set(int x, int y, int val) {
		if (!isSpaceEmpty(x, y)) {
			throw new IllegalStateException("(" + x + "," + y + ") is not empty.");
		}
		val = adaptValueForType(val);
		board[x][y] = val;
		if (type == Type.PLAYER) {
			matrix[coordToOrdinal(x, y)] = (double) val;
		} else {
			tt.check(x, y, val);
		}
	}

	public static int coordToOrdinal(int x, int y) {
		return x + (y * SIZE);
	}

	public static int[] ordinalToCoord(int n) {
		return new int[] { n % SIZE, Math.floorDiv(n, SIZE) };
	}

	public boolean isWin(int val, int addedX, int addedY) {
		if (type != Type.SERVER) {
			throw new IllegalStateException("Must be Server Board!");
		}
		val = adaptValueForType(val);
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

	public boolean isTie() {
		if (type != Type.SERVER) {
			throw new IllegalStateException("Must be Server Board!");
		}
		return tt.free == 0;
	}

	public double[] asDoubleArray() {
		if (type != Type.PLAYER) {
			throw new IllegalStateException("Must be Player Board!");
		}
		return matrix;
	}

	private int adaptValueForType(int n) {
		return type == Type.SERVER ? n + 1 : n;
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	private boolean isTopLeftToBottomRight(int x, int y) {
		return x == y;
	}

	private boolean isBottomLeftToTopRight(int x, int y) {
		return x == (SIZE - y - 1);
	}

	private static final int WIN_COUNTS = (SIZE * 2) + 2;
	private static final int TT_EMPTY = -1;
	private static final int TT_TIE = -2;

	private class TieTracker {

		int[] check;
		int free;

		TieTracker() {
			check = new int[WIN_COUNTS];
			Arrays.fill(check, TT_EMPTY);
			free = WIN_COUNTS;
		}

		void check(int x, int y, int val) {
			if (isTopLeftToBottomRight(x, y))
				checkIndex(WIN_COUNTS - 1, val);
			if (isBottomLeftToTopRight(x, y))
				checkIndex(WIN_COUNTS - 2, val);
			checkIndex(x, val);
			checkIndex(SIZE + y, val);
		}

		void checkIndex(int i, int val) {
			switch (check[i]) {
			case TT_EMPTY:
				check[i] = val;
				break;
			case TT_TIE:
				break;
			default:
				if (check[i] != val) {
					check[i] = TT_TIE;
					free--;
				}
			}
		}

	}

	//////////////////////////////////////////////////////////////////////////////////////////

	// copy from Matrix and adapt for ints
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
