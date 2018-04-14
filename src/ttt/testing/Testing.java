package ttt.testing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import ttt.Board;
import ttt.Game;
import ttt.learning.GameIO;
import ttt.learning.GamePostfix;
import ttt.learning.Matrix;
import ttt.learning.NeuralNetwork;

final class Testing {

	private Testing() {
	}

	private static class TestingException extends RuntimeException {

		private static final long serialVersionUID = -4242822302111372293L;

		public TestingException(String s) {
			super(s);
		}

		public TestingException(Throwable t) {
			super(t);
		}

	}

	public static void main(String[] args) {
		testGameSavingAndLoading();
		testNetworkSavingAndLoading();
	}

	private static void testEquals(int got, int want, String message) {
		if (got != want) {
			throw new TestingException(message + ": " + got + "/" + want);
		}
	}

	private static void testGameSavingAndLoading() {
		final Random rand = new Random();
		final int players = 1 + rand.nextInt(4);
		final int winner = (rand.nextBoolean() && rand.nextBoolean()) ? -1 : rand.nextInt(players);
		final int count = 10 + rand.nextInt(Board.SIZE * (Board.SIZE / 2));
		final int[][] moves = new int[count][3];

		final Game game = new Game(players);
		game.setWinner(winner);
		for (int t = 0; t < count; t++) {
			final int turn = t % players;
			final int x = rand.nextInt(Board.SIZE);
			final int y = rand.nextInt(Board.SIZE);
			moves[t][0] = turn;
			moves[t][1] = x;
			moves[t][2] = y;
			game.recordMove(turn, x, y);
		}

		try {
			File file = GameIO.saveGame(game, GamePostfix.NONE);
			try (FileInputStream fis = new FileInputStream(file); DataInputStream reader = new DataInputStream(fis)) {
				int readCount;
				testEquals(reader.readInt(), players, "Game Players");
				testEquals(reader.readInt(), winner, "Game Winner");
				testEquals(readCount = reader.readInt(), count, "Game Total Moves");
				for (int i = 0; i < readCount; i++) {
					testEquals(reader.readInt(), moves[i][0], "Move " + i + " Player");
					testEquals(reader.readInt(), moves[i][1], "Move " + i + " X");
					testEquals(reader.readInt(), moves[i][2], "Move " + i + " Y");
				}
			}
			if (!file.delete()) {
				throw new TestingException("Couldn't Delete: " + file.getName());
			}
		} catch (IOException ioe) {
			throw new TestingException(ioe);
		}
	}

	private static void testNetworkSavingAndLoading() {
		double[][] xda = new double[9][3];
		double[][] yda = new double[9][1];
		int i = 0;
		xda[i] = new double[] { 3, 2, 1328 / 1000 };
		yda[i++] = new double[] { 209900 };
		xda[i] = new double[] { 4, 3, 2830 / 1000 };
		yda[i++] = new double[] { 799900 };
		xda[i] = new double[] { 4, 2, 2380 / 1000 };
		yda[i++] = new double[] { 359900 };
		xda[i] = new double[] { 5, 6, 5707 / 1000 };
		yda[i++] = new double[] { 1900000 };
		xda[i] = new double[] { 4, 4, 3052 / 1000 };
		yda[i++] = new double[] { 924900 };
		xda[i] = new double[] { 5, 5, 5214 / 1000 };
		yda[i++] = new double[] { 1599000 };
		xda[i] = new double[] { 4, 3, 3532 / 1000 };
		yda[i++] = new double[] { 995000 };
		xda[i] = new double[] { 4, 2, 2069 / 1000 };
		yda[i++] = new double[] { 429900 };
		xda[i] = new double[] { 4, 5, 3518 / 1000 };
		yda[i++] = new double[] { 949900 };

		Matrix x = (new Matrix(xda)).normalize();
		Matrix y = (new Matrix(yda)).normalize(2000000);

		NeuralNetwork nn = new NeuralNetwork(3, 3, 2, 1);

		Matrix out = nn.calculate(x);

		File file = null;
		NeuralNetwork read = null;
		try {
			file = GameIO.saveNetwork(nn);
			read = GameIO.loadNetwork(3, 3, 2, 1);
		} catch (IOException ioe) {
			throw new TestingException(ioe);
		}

		if (!out.equals(read.calculate(x))) {
			throw new TestingException("First output not equal.");
		}

		Matrix[] derivative = nn.costPrime(y);
		Matrix[] weights = nn.getWeights();
		for (int w = 0; w < 3; w++) {
			weights[w] = weights[w].add(derivative[w].negative());
		}
		nn.setWeights(weights);

		out = nn.calculate(x);

		try {
			file = GameIO.saveNetwork(nn);
			read = GameIO.loadNetwork(3, 3, 2, 1);
		} catch (IOException ioe) {
			throw new TestingException(ioe);
		}

		if (!out.equals(read.calculate(x))) {
			throw new TestingException("Second output not equal.");
		}

		Matrix[] readWeights = read.getWeights();
		for (int j = 0; j < weights.length; j++) {
			if (!weights[j].equals(readWeights[j])) {
				throw new TestingException("Weight " + j + " not equal.");
			}
		}

		if (!file.delete()) {
			throw new TestingException("Couldn't Delete: " + file.getName());
		}
	}

}
