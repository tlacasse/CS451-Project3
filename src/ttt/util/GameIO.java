package ttt.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import ttt.learning.AI;
import ttt.learning.Convolutional;
import ttt.learning.Matrix;
import ttt.learning.NeuralNetwork;

/**
 * Deals with saving and loading of Neural Network and Game files.
 */
public final class GameIO {

	public static final String PROJECT_NAME, PROJECT_ROOT, BIN, DIRECTORY_NN, DIRECTORY_GAMES, DIRECTORY_TIES;

	static {
		PROJECT_NAME = "CS451-Project3";
		String path = Paths.get("").toAbsolutePath().toString();
		PROJECT_ROOT = path.substring(0, path.indexOf(PROJECT_NAME) + PROJECT_NAME.length()) + "\\";
		BIN = PROJECT_ROOT + "bin";
		DIRECTORY_NN = PROJECT_ROOT + "data\\";
		DIRECTORY_GAMES = DIRECTORY_NN + "games\\";
		DIRECTORY_TIES = DIRECTORY_NN + "ties\\";
	}

	public static File saveNetwork(AI nn) throws FileNotFoundException, IOException {
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
		final String fileName = DIRECTORY_NN + nn.fileName();
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			fos.write(buffer.array());
		}
		System.out.println("Neural Network Saved: \"" + fileName + "\"");
		return new File(fileName);
	}

	public static AI loadNetwork(String name, boolean isCNN) throws FileNotFoundException, IOException {
		final Matrix[] weights;
		final int[] nodes;
		try (FileInputStream fis = new FileInputStream(DIRECTORY_NN + name);
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
		final AI nn;
		// this ends up being a little less general when adding the CNN
		if (isCNN) {
			// cnn_imgSize_convLayers_conv.nn
			String[] parts = name.split(".")[0].split("_");
			int imgSize = Integer.parseInt(parts[1]);
			int[] conv = TTTUtil.strArrayToIntArray(parts[2].split("-"));
			int[] full = TTTUtil.strArrayToIntArray(parts[3].split("-"));
			nn = new Convolutional(imgSize, conv, full);
		} else {
			nn = new NeuralNetwork(nodes);
		}
		nn.setWeights(weights);
		return nn;
	}

	public static File saveGame(Game game, GamePostfix postfix) throws FileNotFoundException, IOException {
		final String fileName = gameFileName(game.hasWinner() ? DIRECTORY_GAMES : DIRECTORY_TIES, postfix);
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			fos.write(game.toByteBuffer().array());
		}
		System.out.println("Game File Created: \"" + fileName + "\"");
		return new File(fileName);
	}

	public static List<Pair<File, Result>> loadGames() throws FileNotFoundException, IOException {
		final File directory = new File(DIRECTORY_GAMES);
		final List<Pair<File, Result>> list = new LinkedList<>();
		for (File file : directory.listFiles()) {
			list.add(new Pair<>(file, new Result(file)));
		}
		return list;
	}

	public static Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> readGamesForNetworkTraining(
			List<GamePostfix> gamesToExclude) throws FileNotFoundException, IOException {
		final Pair<List<double[]>, List<double[]>> win = new Pair<>(new LinkedList<>(), new LinkedList<>());
		final Pair<List<double[]>, List<double[]>> lose = new Pair<>(new LinkedList<>(), new LinkedList<>());
		for (Pair<File, Result> pair : loadGames()) {
			final String filename = pair.getKey().toString();
			boolean skip = false;
			for (GamePostfix postfix : gamesToExclude) {
				if (filename.indexOf(postfix.value) >= 0) {
					skip = true;
					break;
				}
			}
			if (skip)
				continue;
			final Result game = pair.getValue();
			for (int player = 0; player < game.players; player++) {
				final Board board = new Board(Board.Type.PLAYER);
				final boolean playerIsWinner = (game.winner == player);
				for (int move = 0; move < game.count; move++) {
					final int moveX = game.getMoveX(move);
					final int moveY = game.getMoveY(move);
					if (game.getMovePlayer(move) == player) {
						(playerIsWinner ? win : lose).getKey().add(board.asDoubleArray().clone());
						(playerIsWinner ? win : lose).getValue().add(createDesiredArray(moveX, moveY));
						board.set(moveX, moveY, Player.SELF);
					} else {
						board.set(moveX, moveY, Player.OTHER);
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

	public static String gameFileName(String directory, GamePostfix postfix) {
		final StringBuilder sb = new StringBuilder(directory);
		sb.append(UUID.randomUUID().toString());
		if (postfix != GamePostfix.NONE) {
			sb.append("_").append(postfix.value);
		}
		sb.append(".game");
		return sb.toString();
	}

	private GameIO() {
	}

}