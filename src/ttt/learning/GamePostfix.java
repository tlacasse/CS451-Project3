package ttt.learning;

/**
 * Type dealing with the player-ness of the game, with relevant text to be
 * appended to the game file.
 */
public enum GamePostfix {

	NONE(null), USER_VS_AI("user"), PVP("pvp");

	public final String value;

	private GamePostfix(String s) {
		value = s;
	}

}
