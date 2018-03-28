package ttt.agents;

import java.io.IOException;
import java.nio.file.Paths;

import ttt.StreamRedirect;

public class Spawn implements Runnable {

	private final ProcessBuilder builder;

	public Spawn(int port) {
		builder = new ProcessBuilder("java", "-cp", Paths.get("").toAbsolutePath().toString() + "\\bin",
				"ttt.agents.Player", Integer.toString(port)); // separate by
																// tokens
		builder.redirectErrorStream(true);
	}

	@Override
	public void run() {
		try {
			Process process = builder.start();
			try (StreamRedirect sr = new StreamRedirect(process.getInputStream(), this.toString())) {
				int exitCode = process.waitFor();
				System.out.println(exitCode);
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Thread Failed: " + this + "\n" + e);
			e.printStackTrace();
		}
	}

}
