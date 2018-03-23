package ttt;

import java.io.IOException;
import java.nio.file.Paths;

public class Spawn implements Runnable {

	private final ProcessBuilder builder;

	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread(new Spawn(5));
		t.start();
		t.join();
	}

	public Spawn(int port) {
		builder = new ProcessBuilder("java", "-cp", Paths.get("").toAbsolutePath().toString() + "\\bin", "ttt.Player",
				Integer.toString(port));
		builder.redirectErrorStream(true);
	}

	@Override
	public void run() {
		try {
			Process process = builder.start();
			try (StreamRedirect sr = new StreamRedirect(process.getInputStream(), "Test")) {
				int exitCode = process.waitFor();
				System.out.println(exitCode);
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Thread Failed: " + this + "\n" + e);
			e.printStackTrace();
		}
	}

}