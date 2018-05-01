package ttt.testing;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javafx.util.Pair;
import ttt.Board;
import ttt.util.GameIO;
import ttt.util.Result;
import ttt.util.TTTUtil;

/**
 * Display games in console.
 */
final class GameReader {

	private GameReader() {
	}

	public static void main(String[] args) throws IOException {
		try (Scanner scan = new Scanner(System.in)) {
			System.out.println("Read Files: ");
			for (Pair<File, Result> game : GameIO.loadGames()) {
				System.out.println(game.getKey());
				Result result = game.getValue();
				Board board = new Board(Board.Type.SERVER);
				for (int i = 0; i < result.count; i++) {
					board.set(result.getMoveX(i), result.getMoveY(i), result.getMovePlayer(i));
				}
				System.out.println(board);
				System.out.println("??? Stop?");
				if (TTTUtil.isYes(scan.nextLine())) {
					break;
				}
			}
		}
	}

}
