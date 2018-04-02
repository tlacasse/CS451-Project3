using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web;
using Newtonsoft.Json;

namespace TicTacToe.Controllers {

	public sealed class TTTUtility {

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

	}

}