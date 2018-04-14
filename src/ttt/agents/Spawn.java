package ttt.agents;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ttt.learning.GameIO;

public class Spawn implements Runnable {

	public static Thread newPlayer(int port) {
		return new Thread(new Spawn(port));
	}

	private static final List<Process> PROCESSES = new LinkedList<>();

	// added for WebServer to be able to kill players
	// needed if, for example, couldn't finish web game so stop game server
	public static void killPlayers() {
		for (Process p : PROCESSES) {
			p.destroy();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private final ProcessBuilder builder;

	private Spawn(int port) {
		// separate by tokens
		builder = new ProcessBuilder("java", "-cp", GameIO.BIN, "ttt.agents.Player", Integer.toString(port));
		builder.inheritIO();
	}

	@Override
	public void run() {
		try {
			Process process = builder.start();
			System.out.println("Exit code: " + process.waitFor());
		} catch (IOException | InterruptedException ioeie) {
			System.out.println("Thread Failed: " + this);
			ioeie.printStackTrace();
		}
	}

}
