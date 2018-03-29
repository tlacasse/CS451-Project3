package ttt;

import java.io.IOException;

public final class Program {

	public static final int PORT = 6327;

	public static void main(String[] args) throws IOException {
		Game.start(PORT, 2);
	}

	private Program() {
	}

}
