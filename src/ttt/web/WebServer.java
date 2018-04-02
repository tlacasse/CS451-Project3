package ttt.web;

import java.io.IOException;
import java.net.ServerSocket;

import ttt.agents.SocketSide;

//Server deals with the C# web server, not the actual webpage client
final class WebServer {

	private static final int PORT = 98;

	private static ServerSocket server;
	private static WebClient client;

	public static void main(String[] args) throws IOException {
		server = new ServerSocket(PORT);
		System.out.println(server);
		try {
			client = new WebClient();
			Thread thread = new Thread(client);
			thread.start();
			thread.join();
		} catch (Exception e) {
			server.close();
		}
	}

	private static final class WebClient extends SocketSide implements Runnable {

		public WebClient() throws IOException {
			super();
		}

		@Override
		public void run() {
			try {
				int x = readInt();
				int y = readInt();
				System.out.println(x + ", " + y);
				close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		@Override
		protected void connect(int port) throws IOException {
			if (socket == null || socket.isClosed()) {
				socket = server.accept();
				System.out.println(socket);
			}
		}

	}
}
