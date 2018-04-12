package ttt;

public class Board {

	public static enum Type {
		/**
		 * <ul>
		 * <li>0 = empty</li>
		 * <li>1 = player 0</li>
		 * <li>2 = player 1</li>
		 * </ul>
		 */
		SERVER,
		/**
		 * <ul>
		 * <li>0 = empty</li>
		 * <li>1 = player move</li>
		 * <li>-1 = other player move</li>
		 * </ul>
		 */
		PLAYER;
	}

	public static final int EMPTY = 0;

	public static final int SIZE = 13;
	public static final int CELLS = SIZE * SIZE;

	private final int[][] board;
	private final double[] matrix;
	private final Type type;
	private int moves;

	public Board(Type type) {
		this.type = type;
		board = new int[SIZE][SIZE];
		matrix = new double[CELLS];
		moves = 0;
	}

	public boolean isFull() {
		return moves == CELLS;
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

	public double[] asDoubleArray() {
		return matrix;
	}

	private int adaptValueForType(int n) {
		return type == Type.SERVER ? n + 1 : n;
	}

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
