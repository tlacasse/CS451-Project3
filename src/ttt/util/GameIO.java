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

	/////////////////////////////////////////////////////////////

	// "magic numbers"
	private static byte CODE_NN = 'N';
	private static byte CODE_CNN = 'C';

	public static File saveNetwork(AI nn) throws FileNotFoundException, IOException {
		if (nn instanceof NeuralNetwork) {
			return saveNetwork0((NeuralNetwork) nn);
		}
		if (nn instanceof Convolutional) {
			return saveNetwork0((Convolutional) nn);
		}
		throw new IllegalArgumentException();
	}

	private static File saveNetwork0(NeuralNetwork nn) throws FileNotFoundException, IOException {
		final Matrix[] weights = nn.getWeights();
		int bufferSize = 1 + Integer.BYTES; // code + #layers
		bufferSize += ((weights.length + 1) * Integer.BYTES); // layerSizes
		bufferSize += getWeightsBufferSize(weights);
		final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		buffer.put(CODE_NN);
		buffer.putInt(weights.length + 1);
		for (int i = 0; i <= weights.length; i++) {
			buffer.putInt(nn.getNodeCount(i));
		}
		writeWeightsToBuffer(buffer, weights);
		return saveNetwork1(nn, buffer);
	}

	private static File saveNetwork0(Convolutional nn) throws FileNotFoundException, IOException {
		final Matrix[] weights = nn.getWeights();
		int bufferSize = 1 + (3 * Integer.BYTES); // 1 + imageWidth +
													// #convLayers +
		// #fullLayers
		bufferSize += nn.convLayers() * Integer.BYTES;
		bufferSize += nn.fullLayers() * Integer.BYTES;
		bufferSize = bufferSize += getWeightsBufferSize(weights);
		final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		buffer.put(CODE_CNN);
		buffer.putInt(nn.imageSize());
		buffer.putInt(nn.convLayers());
		buffer.putInt(nn.fullLayers());
		for (int i = 0; i < nn.convLayers(); i++) {
			buffer.putInt(nn.getConvLayerSize(i));
		}
		for (int i = 0; i < nn.fullLayers(); i++) {
			buffer.putInt(nn.getFullLayerSize(i));
		}
		writeWeightsToBuffer(buffer, weights);
		return saveNetwork1(nn, buffer);
	}

	private static File saveNetwork1(AI nn, ByteBuffer buffer) throws FileNotFoundException, IOException {
		final String fileName = DIRECTORY_NN + nn.fileName();
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			fos.write(buffer.array());
		}
		System.out.println(nn.getClass().getSimpleName() + " Saved: \"" + fileName + "\"");
		return new File(fileName);
	}

	private static void writeWeightsToBuffer(ByteBuffer buffer, Matrix[] weights) {
		buffer.putInt(weights.length);
		for (Matrix m : weights) {
			buffer.putInt(m.rows());
			buffer.putInt(m.columns());
		}
		for (Matrix m : weights) {
			for (int i = 0; i < m.rows(); i++) {
				for (int j = 0; j < m.columns(); j++) {
					buffer.putDouble(m.get(i, j));
				}
			}
		}
	}

	private static int getWeightsBufferSize(Matrix[] weights) {
		int size = Integer.BYTES; // number of weights
		size += weights.length * 2 * Integer.BYTES; // dimensions of each
		for (Matrix m : weights) {
			size += m.rows() * m.columns() * Double.BYTES; // data in each
		}
		return size;
	}

	/////////////////////////////////////////////////////////////

	public static AI loadNetwork(String name) throws FileNotFoundException, IOException {
		AI nn = null;
		try (FileInputStream fis = new FileInputStream(DIRECTORY_NN + name);
				DataInputStream reader = new DataInputStream(fis)) {
			final byte code = reader.readByte();
			if (code == CODE_NN) {
				nn = loadNetworkNN(reader);
			}
			if (code == CODE_CNN) {
				nn = loadNetworkCNN(reader);
			}
		}
		return nn;
	}

	public static AI loadNetworkNN(DataInputStream reader) throws FileNotFoundException, IOException {
		final Matrix[] weights;
		final int[] nodes;

		final int layers = reader.readInt();
		nodes = new int[layers];
		for (int i = 0; i < layers; i++) {
			nodes[i] = reader.readInt();
		}
		weights = readWeights(reader);

		AI nn = new NeuralNetwork(nodes);
		nn.setWeights(weights);
		return nn;
	}

	public static AI loadNetworkCNN(DataInputStream reader) throws FileNotFoundException, IOException {
		final Matrix[] weights;
		final int imageSize, conv, full;
		final int[] convLayers, fullLayers;

		imageSize = reader.readInt();
		conv = reader.readInt();
		full = reader.readInt();
		convLayers = new int[conv];
		for (int i = 0; i < conv; i++) {
			convLayers[i] = reader.readInt();
		}
		fullLayers = new int[full];
		for (int i = 0; i < full; i++) {
			fullLayers[i] = reader.readInt();
		}
		weights = readWeights(reader);

		AI cnn = new Convolutional(imageSize, convLayers, fullLayers);
		cnn.setWeights(weights);
		return cnn;
	}

	private static Matrix[] readWeights(DataInputStream reader) throws FileNotFoundException, IOException {
		final Matrix[] weights = new Matrix[reader.readInt()];
		final int[][] dims = new int[weights.length][2];
		for (int k = 0; k < weights.length; k++) {
			dims[k][0] = reader.readInt();
			dims[k][1] = reader.readInt();
		}
		for (int k = 0; k < weights.length; k++) {
			final double[][] ds = new double[dims[k][0]][dims[k][1]];
			for (int i = 0; i < dims[k][0]; i++) {
				for (int j = 0; j < dims[k][1]; j++) {
					ds[i][j] = reader.readDouble();
				}
			}
			weights[k] = new Matrix(ds);
		}
		return weights;
	}

	/////////////////////////////////////////////////////////////

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

	/////////////////////////////////////////////////////////////

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
