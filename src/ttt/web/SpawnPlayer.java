package ttt.web;

import ttt.Program;
import ttt.agents.Spawn;

final class SpawnPlayer {

	public static void main(String[] args) {
		Program.join(Spawn.newPlayer(WebServer.PORT));
	}

}
