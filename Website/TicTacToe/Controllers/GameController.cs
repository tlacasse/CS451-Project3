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

		[HttpPost]
		[Route("post/move")]
		public HttpResponseMessage sendMove() {
			Move move = TTTUtility.readRequest<Move>();
			Connection.writeInt(move.x);
			Connection.writeInt(move.y);
			Connection.send();
			return Request.CreateResponse(HttpStatusCode.Accepted);
		}

	}

}
