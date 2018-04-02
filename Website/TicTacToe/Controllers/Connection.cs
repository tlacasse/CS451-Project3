using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Web;

namespace TicTacToe.Controllers {

	public sealed class Connection {
		//https://docs.microsoft.com/en-us/dotnet/framework/network-programming/synchronous-client-socket-example

		private static readonly string LOCALHOST = "127.0.0.1";
		private static readonly int PORT = 98;
		private static readonly int BUFFER_SIZE = 2 * 4;

		private static Socket socket;
		private static MemoryStream buffer;

		public static void start() {
			socket = new Socket(SocketType.Stream, ProtocolType.Tcp);
			socket.Connect(new IPEndPoint(IPAddress.Parse(LOCALHOST), PORT));
			buffer = new MemoryStream(BUFFER_SIZE);
		}

		public static void send() {
			socket.Send(buffer.GetBuffer());
			buffer.Seek(0, SeekOrigin.Begin);
		}

		public static void writeInt(int i) {
			byte[] bytes = BitConverter.GetBytes(i);
			buffer.WriteByte(bytes[3]);
			buffer.WriteByte(bytes[2]);
			buffer.WriteByte(bytes[1]);
			buffer.WriteByte(bytes[0]);
		}

		public static void end() {
			socket.Close();
			buffer.Close();
		}

	}

}