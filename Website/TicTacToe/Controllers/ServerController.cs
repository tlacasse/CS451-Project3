using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace TicTacToe.Controllers {

	[RoutePrefix("server")]
	public class ServerController : ApiController {

		[HttpGet]
		[Route("kill")]
		public string kill() {
			TTTUtility.stopJavaServer();
			return "'pls dont kill me' - the server you just killed."; // ha
		}

		[HttpGet]
		[Route("start")]
		public string start() {
			TTTUtility.startJavaServer(false);
			return "started server";
		}

		[HttpGet]
		[Route("startAI")]
		public string startAI() {
			TTTUtility.startJavaServer(true);
			return "started server - with AI";
		}

	}

}
