package ttt.learning;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class GameIO {

	public final static String DIRECTORY_WEIGHTS = Paths.get("").toAbsolutePath().toString() + "\\data\\";
	public final static String DIRECTORY_GAMES = DIRECTORY_WEIGHTS + "games\\";

	public static void saveNetwork(NeuralNetwork nn) throws IOException {
		final Matrix[] weights = nn.getWeights();
		final int[] nodes = new int[weights.length + 1];
		int bufferSize = Integer.BYTES;

		for (int i = 0; i < weights.length; i++) {
			nodes[i] = weights[i].rows();
			weights[i].display();
			bufferSize += Integer.BYTES + (weights[i].rows() * weights[i].columns() * Double.BYTES);
		}
		nodes[nodes.length - 1] = weights[weights.length - 1].columns();
		bufferSize += Integer.BYTES;

		// #layers (int), sizeOfEachLayer (int), weights (doubles)
		final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		System.out.println(bufferSize);
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

	public static NeuralNetwork loadNetwork(int... nodes) throws IOException {
		if (nodes.length < 3) {
			throw new IllegalArgumentException("Must have at least 3 layers.");
		}
		final Matrix[] weights;
		try (FileInputStream fis = new FileInputStream(nnFileName(nodes));
				DataInputStream reader = new DataInputStream(fis)) {
			// recreate structure just to be sure
			final int layers = reader.readInt();
			System.out.println(layers);
			nodes = new int[layers];
			for (int i = 0; i < layers; i++) {
				nodes[i] = reader.readInt();
				System.out.println(nodes[i]);
			}
			weights = new Matrix[layers - 1];
			for (int k = 0; k < layers - 1; k++) {
				final double[][] ds = new double[nodes[k]][nodes[k] + 1];
				for (int i = 0; i < nodes[k]; i++) {
					for (int j = 0; j < nodes[k] + 1; j++) {
						ds[i][j] = reader.readDouble();
						System.out.println(ds[i][j]);
					}
				}
				weights[k] = new Matrix(ds);
			}
		}
		final NeuralNetwork nn = new NeuralNetwork(nodes);
		nn.setWeights(weights);
		return nn;
	}

	private static String nnFileName(int... nodes) {
		final StringBuffer sb = new StringBuffer(DIRECTORY_WEIGHTS + "weights_");
		for (int i = 0; i < nodes.length - 1; i++) {
			sb.append(nodes[i]).append("-");
		}
		sb.append(nodes[nodes.length - 1]).append(".nn");
		return sb.toString();
	}

}
