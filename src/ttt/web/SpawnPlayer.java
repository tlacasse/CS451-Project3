package ttt.web;

import ttt.agents.Spawn;

final class SpawnPlayer {

	// just for adding extra AI players to the website game.
	public static void main(String[] args) throws InterruptedException {
		final Thread t = Spawn.newPlayer(WebServer.PORT);
		t.start();
		t.join();
	}

}
