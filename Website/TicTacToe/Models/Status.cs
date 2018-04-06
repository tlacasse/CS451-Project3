using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace TicTacToe.Models {

	public class Status {

		public byte code { get; private set; }
		public int x { get; private set; }
		public int y { get; private set; }

		public Status(byte code, int x, int y) {
			this.code = code;
			this.x = x;
			this.y = y;
		}

		public Status(byte code) : this(code, -1, -1) {
		}

		public Status() : this(99) {
		}

	}

}