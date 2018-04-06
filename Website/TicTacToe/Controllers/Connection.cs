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

		public static bool started { get; private set; }

		private static readonly string LOCALHOST = "127.0.0.1";
		private static readonly int PORT = 98;
		private static readonly int BUFFER_SIZE = 1 + (2 * 4);

		private static Socket socket;
		private static MemoryStream buffer;

		private static byte[] read;
		private static int readInc;

		public static void start() {
			socket = new Socket(SocketType.Stream, ProtocolType.Tcp);
			socket.Connect(new IPEndPoint(IPAddress.Parse(LOCALHOST), PORT));
			buffer = new MemoryStream(BUFFER_SIZE);
			read = new byte[BUFFER_SIZE];
			started = true;
		}

		public static void end() {
			if (started) {
				socket.Close();
				buffer.Close();
				started = false;
			}
		}

		public static void send() {
			socket.Send(buffer.GetBuffer());
			buffer.Seek(0, SeekOrigin.Begin);
		}

		public static void writeByte(byte b) {
			buffer.WriteByte(b);
		}

		public static void writeInt(int i) {
			byte[] bytes = BitConverter.GetBytes(i);
			if (BitConverter.IsLittleEndian) {
				Array.Reverse(bytes);
			}
			buffer.WriteByte(bytes[0]);
			buffer.WriteByte(bytes[1]);
			buffer.WriteByte(bytes[2]);
			buffer.WriteByte(bytes[3]);
		}

		public static void beginRead() {
			socket.Receive(read);
			readInc = 0;
		}

		public static byte readByte() {
			return read[readInc++];
		}

		public static int readInt() {
			byte[] bytes = new byte[] { readByte(), readByte(), readByte(), readByte() };
			if (BitConverter.IsLittleEndian) {
				Array.Reverse(bytes);
			}
			return BitConverter.ToInt32(bytes, 0);
		}

		public static bool hasData() {
			return socket.Available > 0;
		}

	}

}