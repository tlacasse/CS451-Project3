package ttt.web;

import ttt.agents.Spawn;

final class SpawnPlayer {

	public static void main(String[] args) throws InterruptedException {
		final Thread t = Spawn.newPlayer(WebServer.PORT);
		t.start();
		t.join();
	}

}
