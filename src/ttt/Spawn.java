package ttt;

import java.io.IOException;

public class Spawn implements Runnable {

	private final ProcessBuilder builder;

	public Spawn(int port) {
		builder = new ProcessBuilder(Integer.toString(port));
	}

	@Override
	public void run() {
		try {
			Process process = builder.start();
			int exitCode = process.waitFor();
			System.out.println(exitCode);
		} catch (IOException | InterruptedException e) {
			System.out.println("Thread Failed: " + this + "\n" + e);
			e.printStackTrace();
		}
	}

}
