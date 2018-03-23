package ttt;

import java.io.InputStream;
import java.util.Scanner;

public class StreamRedirect implements AutoCloseable {

	private final String prefix;
	private final Thread thread;
	private final Scanner scanner;
	private boolean end;

	public StreamRedirect(InputStream stream, String prefix) {
		this.prefix = prefix;
		end = false;
		scanner = new Scanner(stream);
		thread = new Thread(new RedirectThread());
		thread.start();
	}

	@Override
	public void close() throws InterruptedException {
		end = true;
		try {
			thread.join();
			scanner.close();
		} catch (InterruptedException ie) {
			scanner.close();
			throw ie;
		}
	}

	private class RedirectThread implements Runnable {
		@Override
		public void run() {
			while (!end) {
				if (scanner.hasNextLine()) {
					System.out.println(prefix + ": " + scanner.nextLine());
				}
			}
		}
	}

}