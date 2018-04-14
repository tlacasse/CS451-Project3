package ttt.web;

import ttt.Program;
import ttt.agents.Spawn;

final class SpawnPlayer {

	public static void main(String[] args) {
		final Thread t = Spawn.newPlayer(WebServer.PORT);
		t.start();
		Program.join(t);
	}

}
