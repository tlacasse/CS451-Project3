using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Web;
using Newtonsoft.Json;

namespace TicTacToe.Controllers {

	public static class TTTUtility {

		public static T readRequest<T>() {
			HttpRequest request = HttpContext.Current.Request;
			string result;
			using (Stream stream = request.InputStream) {
				using (StreamReader streamReader = new StreamReader(stream, request.ContentEncoding)) {
					result = streamReader.ReadToEnd();
				}
			}
			return JsonConvert.DeserializeObject<T>(result);
		}

		public static readonly int PORT_OPEN = 97;
		public static readonly int PORT_CLOSE = 96;

		public static void startJavaServer() {
			doJavaServer(PORT_OPEN);
		}

		public static void stopJavaServer() {
			doJavaServer(PORT_CLOSE);
		}

		private static void doJavaServer(int port) {
			TcpClient socket = new TcpClient(Connection.LOCALHOST, port);
			socket.Close();
		}

	}

}