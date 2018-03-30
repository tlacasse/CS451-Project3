package ttt.learning;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javafx.util.Pair;
import ttt.Board;
import ttt.Game;
import ttt.agents.Player;

public final class GameIO {

	public static final String DIRECTORY_NN = Paths.get("").toAbsolutePath().toString() + "\\data\\";
	public static final String DIRECTORY_GAMES = DIRECTORY_NN + "games\\";
	public static final String DIRECTORY_TIES = DIRECTORY_NN + "ties\\";
	public static final String DIRECTORY_TRAINING = DIRECTORY_NN + "training\\";

	public static void saveNetwork(NeuralNetwork nn) throws IOException {
		final Matrix[] weights = nn.getWeights();
		final int[] nodes = new int[weights.length + 1];
		int bufferSize = Integer.BYTES;

		for (int i = 0; i < weights.length; i++) {
			nodes[i] = weights[i].rows();
			bufferSize += Integer.BYTES + (weights[i].rows() * weights[i].columns() * Double.BYTES);
		}
		nodes[nodes.length - 1] = weights[weights.length - 1].columns();
		bufferSize += Integer.BYTES;

		// #layers (int), sizeOfEachLayer (ints), weights (doubles)
		final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		try (FileOutputStream fos = new FileOutputStream(nnFileName(nodes))) {
			buffer.putInt(nodes.length);
			for (Matrix m : weights) {
				buffer.putInt(m.rows());
			}
			buffer.putInt(weights[weights.length - 1].columns());
			for (Matrix m : weights) {
				for (int i = 0; i < m.rows(); i++) {
					for (int j = 0; j < m.columns(); j++) {
						buffer.putDouble(m.get(i, j));
					}
				}
			}
			fos.write(buffer.array());
		}
	}

	public static NeuralNetwork loadNetwork(int... nameNodes) throws IOException {
		if (nameNodes.length < 3) {
			throw new IllegalArgumentException("Must have at least 3 layers.");
		}
		final Matrix[] weights;
		final int[] nodes;
		try (FileInputStream fis = new FileInputStream(nnFileName(nameNodes));
				DataInputStream reader = new DataInputStream(fis)) {
			// recreate structure just to be sure things are correct
			final int layers = reader.readInt();
			nodes = new int[layers];
			for (int i = 0; i < layers; i++) {
				nodes[i] = reader.readInt();
			}
			weights = new Matrix[layers - 1];
			for (int k = 0; k < layers - 1; k++) {
				final double[][] ds = new double[nodes[k]][nodes[k + 1]];
				for (int i = 0; i < nodes[k]; i++) {
					for (int j = 0; j < nodes[k + 1]; j++) {
						ds[i][j] = reader.readDouble();
					}
				}
				weights[k] = new Matrix(ds);
			}
		}
		final NeuralNetwork nn = new NeuralNetwork(nodes);
		nn.setWeights(weights);
		return nn;
	}

	public static void saveGame(Game game) throws IOException {
		final String fileName = gameFileName(game.hasWinner() ? DIRECTORY_GAMES : DIRECTORY_TIES);
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			fos.write(game.toByteBuffer().array());
		}
		System.out.println("Game File Created: \"" + fileName + "\"");
	}

	public static Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> readGamesForNetworkTraining() throws IOException {
		final File directory = new File(DIRECTORY_GAMES);
		final Pair<List<double[]>, List<double[]>> win = new Pair<>(new LinkedList<>(), new LinkedList<>());
		final Pair<List<double[]>, List<double[]>> lose = new Pair<>(new LinkedList<>(), new LinkedList<>());
		for (File file : directory.listFiles()) {
			try (FileInputStream fis = new FileInputStream(file); DataInputStream reader = new DataInputStream(fis)) {
				final int playerCount = reader.readInt();
				final int winner = reader.readInt();
				final int moveCount = reader.readInt();
				for (int player = 0; player < playerCount; player++) {
					final Board board = new Board(false);
					for (int i = 0; i < moveCount; i++) {
						final boolean moveIsCurrentPlayer = (reader.readInt() == player);
						final boolean playerIsWinner = (winner == player);
						final int moveX = reader.readInt();
						final int moveY = reader.readInt();
						if (moveIsCurrentPlayer) {
							(playerIsWinner ? win : lose).getKey().add(board.getDoubleArray()); // input
							board.set(moveX, moveY, Player.SELF);
							(playerIsWinner ? win : lose).getValue().add(createDesiredArray(moveX, moveY)); // output
						} else {
							board.set(moveX, moveY, Player.OTHER);
						}
					}
				}
			}
		}
		return new Pair<>(
				new Pair<>(new Matrix(win.getKey().toArray(new double[win.getKey().size()][Board.CELLS])),
						new Matrix(win.getValue().toArray(new double[win.getValue().size()][Board.CELLS]))),
				new Pair<>(new Matrix(lose.getKey().toArray(new double[lose.getKey().size()][Board.CELLS])),
						new Matrix(lose.getValue().toArray(new double[lose.getValue().size()][Board.CELLS]))));
	}

	private static double[] createDesiredArray(int x, int y) {
		final double[] result = new double[Board.CELLS];
		result[Board.coordToOrdinal(x, y)] = 1.0;
		return result;
	}

	private static String nnFileName(int... nodes) {
		final StringBuilder sb = new StringBuilder(DIRECTORY_NN);
		sb.append("nn_");
		for (int i = 0; i < nodes.length - 1; i++) {
			sb.append(nodes[i]).append("-");
		}
		sb.append(nodes[nodes.length - 1]).append(".nn");
		return sb.toString();
	}

	private static String gameFileName(String directory) {
		final StringBuilder sb = new StringBuilder(directory);
		sb.append(UUID.randomUUID().toString());
		sb.append(".game");
		return sb.toString();
	}

	private GameIO() {
	}

}
