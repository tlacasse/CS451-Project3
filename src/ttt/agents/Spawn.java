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
		builder.inheritIO();
	}

	@Override
	public void run() {
		try {
			Process process = builder.start();
			try (StreamRedirect sr = new StreamRedirect(process.getInputStream(), this.toString())) {
				System.out.println(this + " exit code: " + process.waitFor());
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Thread Failed: " + this + "\n" + e);
			e.printStackTrace();
		}
	}

	private String strOut = null;

	@Override
	public String toString() {
		return strOut == null ? strOut = super.toString().substring(getClass().getPackage().getName().length() + 1)
				: strOut;
	}

}
