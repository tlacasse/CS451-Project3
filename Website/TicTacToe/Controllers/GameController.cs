using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;
using Newtonsoft.Json;
using TicTacToe.Models;

namespace TicTacToe.Controllers {

	[RoutePrefix("api")]
	public class GameController : ApiController {

		private static readonly byte TURN = 0;
		private static readonly byte OTHER_PLAYER_MOVE = 1;
		private static readonly byte GAME_DONE = 2;
		private static readonly byte MOVE = 3;
		private static readonly byte FULL_BOARD = 4;

		private static readonly byte FIRST_PLAYER = 5;
		private static readonly byte START_GAME = 6;
		private static readonly byte CONNECTED = 7;

		[HttpGet]
		[Route("connect")]
		public int connect() {
			Connection.start();
			Connection.beginRead();
			return Connection.readByte();
		}

		[HttpGet]
		[Route("start")]
		public int start() {
			Connection.writeByte(START_GAME);
			Connection.send();
			return 0; //don't actually care what it is, but not POST because no data being sent from web client
		}

		[HttpPost]
		[Route("move")]
		public HttpResponseMessage move() {
			Move move = TTTUtility.readRequest<Move>();
			Connection.writeByte(MOVE);
			Connection.writeInt(move.x);
			Connection.writeInt(move.y);
			Connection.send();
			return Request.CreateResponse(HttpStatusCode.Accepted);
		}

		[HttpGet]
		[Route("status")]
		public Status status() {
			if (Connection.hasData()) {
				Connection.beginRead();
				byte code = Connection.readByte();
				if (code == OTHER_PLAYER_MOVE) {
					int x = Connection.readInt();
					int y = Connection.readInt();
					return new Status(code, x, y);
				}
				return new Status(code);
			}
			return new Status();
		}

	}

}
