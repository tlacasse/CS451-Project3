using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Sockets;
using System.Threading;
using System.Web;
using System.Web.Http;
using Newtonsoft.Json;
using TicTacToe.Models;

namespace TicTacToe.Controllers {

	[RoutePrefix("api")]
	public class GameController : ApiController {

		//		private static readonly byte TURN = 0;
		private static readonly byte OTHER_PLAYER_MOVE = 1;
		//		private static readonly byte GAME_DONE = 2;
		private static readonly byte MOVE = 3;
		//		private static readonly byte FULL_BOARD = 4;

		//		private static readonly byte FIRST_PLAYER = 5;
		private static readonly byte START_GAME = 6;
		//		private static readonly byte CONNECTED = 7;

		[HttpGet]
		[Route("connect")]
		public int connect() {
			startConnection();
			return connection.readByte();
		}

		[HttpPost]
		[Route("start")]
		public HttpResponseMessage start() {
			connection.writeByte(START_GAME);
			return Request.CreateResponse(HttpStatusCode.Created);
		}

		[HttpPost]
		[Route("move")]
		public HttpResponseMessage move() {
			Move move = TTTUtility.readRequest<Move>();
			connection.writeByte(MOVE);
			connection.writeInt(move.x);
			connection.writeInt(move.y);
			return Request.CreateResponse(HttpStatusCode.Accepted);
		}

		[HttpGet]
		[Route("status")]
		public Status status() {
			if (connection.hasData()) {
				byte code = connection.readByte();
				if (code == OTHER_PLAYER_MOVE) {
					int x = connection.readInt();
					int y = connection.readInt();
					return new Status(code, x, y);
				}
				return new Status(code);
			}
			return new Status();
		}

		public static readonly Dictionary<string, Connection> connections = new Dictionary<string, Connection>();

		private static string key() {
			return HttpContext.Current.Request.UserHostAddress;
		}

		private Connection connection {
			get {
				return connections[key()];
			}
		}

		private void startConnection() {
			if (connections.ContainsKey(key())) {
				connections[key()].Dispose();
				connections.Remove(key());
			}
			try {
				tryConnect(2);
				return;
			}
			catch (SocketException se) {
				TTTUtility.startJavaServer();
			}
			tryConnect(2);
		}

		private void tryConnect(int tries) {
			for (int t = tries - 1; t >= 0; t--) {
				try {
					Connection c = new Connection();
					connections.Add(key(), c);
					return;
				}
				catch (SocketException se) {
					if (t == 0) {
						throw se;
					}
				}
				Thread.Sleep(2000);
			}
		}

	}

}
