package ttt.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ttt.Board;
import ttt.learning.Convolutional;
import ttt.learning.Matrix;
import ttt.util.GameIO;
import ttt.util.Result;

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

		double[][] yda = new double[3][169];
		yda[0][5] = 1.0;
		yda[0][50] = 1.0;
		yda[0][127] = 1.0;
		Matrix y = new Matrix(yda);

		Convolutional cnn = new Convolutional(Board.SIZE, new int[] { 3, 5 }, new int[] { 50, Board.CELLS });
		cnn.calculate(x).display();

		System.out.println("\n\n\n");
		Matrix[] dCdW = cnn.costPrime(y);
		System.out.println("\n\n\n--------------------------------------------");
		for (Matrix m : dCdW) {
			if (m != null)
				System.out.println(m.rows() + " x " + m.columns());
			else
				System.out.println("null");
		}
		// dCdW[0].display();
		// dCdW[1].display();
		// dCdW[2].display();
	}

}
