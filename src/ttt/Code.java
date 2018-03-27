package ttt;

public final class Code {

	// complains about enums, 'switch statements must be constant expressions'
	// so class it is

	public static final byte TURN = 0;
	public static final byte OTHER_PLAYER_MOVE = 1;
	public static final byte GAME_DONE = 2;
	public static final byte MOVE = 3;
	public static final byte FULL_BOARD = 4;

	private Code() {

	}

}
