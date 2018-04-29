package ttt.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ttt.Board;
import ttt.learning.Convolutional;
import ttt.learning.GameIO;
import ttt.learning.Matrix;
import ttt.learning.Result;

final class ConvNetTest {

	private ConvNetTest() {
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		final File directory = new File(GameIO.DIRECTORY_GAMES);
		final int numCases = 3;
		double[][] cases = new double[numCases][];
		for (int c = 0; c < numCases; c++) {
			Result game = new Result(directory.listFiles()[c]);
			Board board = new Board(Board.Type.PLAYER);
			for (int i = 0; i < game.count; i++) {
				board.set(game.getMoveX(i), game.getMoveY(i), game.getMovePlayer(i));
			}
			cases[c] = board.asDoubleArray();
		}
		Matrix x = new Matrix(cases);

		Convolutional cnn = new Convolutional(Board.SIZE, new int[] { 5, 5 }, new int[] { 50, Board.CELLS });
		cnn.calculate(x).display();
	}

}
