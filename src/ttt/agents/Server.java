package ttt.agents;

import java.io.IOException;
import java.net.ServerSocket;

public class Server implements AutoCloseable, Runnable {

	private final ServerSocket server;
	private final Client[] clients;
	private final int totalClients;

	public Server(int port) throws IOException {
		totalClients = 2;

		server = new ServerSocket(port);
		System.out.println(this);

		clients = new Client[totalClients];
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < totalClients; i++) {
				clients[i] = new Client(0);
			}
		} catch (IOException e) {
			System.out.println("Thread Failed: " + this);
			e.printStackTrace();
		}

	}

	@Override
	public void close() throws IOException {
		for (Client client : clients) {
			if (client != null) {
				client.close();
			}
		}
		server.close();
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	private static int clientIdInc = 0;

	private class Client extends SocketSide {

		private final int id;

		public Client(int bufferSize) throws IOException {
			super(bufferSize);
			id = clientIdInc++;
		}

		@Override
		protected void connect(int port) throws IOException {
			if (socket == null || socket.isClosed()) {
				socket = server.accept();
				System.out.println(this);
			}
		}

	}

}
