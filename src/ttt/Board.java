package ttt;

public class Board {

	public static final byte X = 1;
	public static final byte O = -1;
	public static final byte EMPTY = 0;

	public static final int SIZE = 13;

	private final byte[][] board;

	public Board() {
		board = new byte[SIZE][SIZE];
	}

	public boolean set(int x, int y, byte val) {
		board[x][y] = val;
		return isWin(val, x, y);
	}

	private boolean isWin(byte val, int addedX, int addedY) {
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
