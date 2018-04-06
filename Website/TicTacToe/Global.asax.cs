using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http;
using System.Web.Mvc;
using System.Web.Optimization;
using System.Web.Routing;
using TicTacToe.Controllers;

namespace TicTacToe {

	public class WebApiApplication : HttpApplication {

		protected void Application_Start() {
			GlobalConfiguration.Configure(WebApiConfig.Register);
		}

		protected void Application_End() {
			Connection.end();
		}

	}

}
