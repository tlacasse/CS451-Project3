using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Web;

namespace TicTacToe.Controllers {

	public class Connection : IDisposable {

		public static readonly string LOCALHOST = "127.0.0.1";
		public static readonly int PORT = 98;

		private readonly TcpClient socket;
		private readonly NetworkStream stream;

		public Connection() {
			socket = new TcpClient(LOCALHOST, PORT);
			stream = socket.GetStream();
		}

		public void Dispose() {
			socket.Close();
		}

		public void writeByte(byte b) {
			stream.WriteByte(b);
		}

		public void writeInt(int i) {
			byte[] bytes = BitConverter.GetBytes(i);
			if (BitConverter.IsLittleEndian) {
				Array.Reverse(bytes);
			}
			writeByte(bytes[0]);
			writeByte(bytes[1]);
			writeByte(bytes[2]);
			writeByte(bytes[3]);
		}

		public byte readByte() {
			return (byte)stream.ReadByte();
		}

		public int readInt() {
			byte[] bytes = new byte[] { readByte(), readByte(), readByte(), readByte() };
			if (BitConverter.IsLittleEndian) {
				Array.Reverse(bytes);
			}
			return BitConverter.ToInt32(bytes, 0);
		}

		public bool hasData() {
			return socket.Available > 0;
		}

	}

}