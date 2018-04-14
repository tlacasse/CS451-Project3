package ttt.agents;

import java.io.IOException;
import ttt.learning.GameIO;

public class Spawn implements Runnable {

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
