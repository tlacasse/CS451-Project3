using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace TicTacToe.Models {

	public class Move {

		public readonly int x;
		public readonly int y;

		public Move(int x, int y) {
			this.x = x;
			this.y = y;
		}

	}

}