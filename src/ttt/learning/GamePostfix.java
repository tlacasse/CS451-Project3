package ttt.learning;

public enum GamePostfix {

	NONE(null), USER_VS_AI("user"), PVP("pvp");

	public final String value;

	private GamePostfix(String s) {
		value = s;
	}

}
