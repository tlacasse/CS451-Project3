package ttt.agents;

import java.io.IOException;
import java.nio.file.Paths;

public class Spawn implements Runnable {

	private final ProcessBuilder builder;

	public Spawn(int port) {
		builder = new ProcessBuilder("java", "-cp", Paths.get("").toAbsolutePath().toString() + "\\bin",
				"ttt.agents.Player", Integer.toString(port)); // separate by
																// tokens
		builder.inheritIO();
	}

	@Override
	public void run() {
		try {
			Process process = builder.start();
			System.out.println("Exit code: " + process.waitFor());
		} catch (IOException | InterruptedException ioeie) {
			System.out.println("Thread Failed: " + this + ioeie);
			ioeie.printStackTrace();
		}
	}

}
