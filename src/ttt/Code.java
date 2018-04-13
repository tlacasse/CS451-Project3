package ttt;

public final class Code {

	// complains about enums, 'switch statements must be constant expressions'
	// so class it is
	//
	//
	//

	public static final byte TURN = 0;
	public static final byte OTHER_PLAYER_MOVE = 1;
	public static final byte GAME_DONE = 2;
	public static final byte MOVE = 3;
	public static final byte GAME_TIE = 4;

	// only for Website
	public static final byte FIRST_PLAYER = 5;
	public static final byte START_GAME = 6;
	public static final byte CONNECTED = 7;

	private Code() {
	}

}
