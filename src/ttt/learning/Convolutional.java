package ttt.learning;

import static ttt.Program.coordToOrdinal;
import static ttt.learning.MatrixMethod.TRANSPOSE;

/**
 * NOT really a convolutional neural network, because I can't figure those out.
 * It is somewhere in between. This may be better due to a game board being more
 * image like.
 */
public class Convolutional {

	private final int[][] sizes;
	private final int[] nodes;
	private Matrix[][] layers, weights;

	public Convolutional(int[][] sizes, int... nodes) {
		checkConstructorArgs(sizes, nodes);
		this.sizes = sizes;
		this.nodes = nodes;
		weights = new Matrix[10][10];
		for (int i = 0; i < sizes.length; i++) {

		}
	}

	public Matrix calculate(Matrix x) throws IllegalArgumentException, ReflectiveOperationException {
		layers[0] = new Matrix[] { x };
		Parallel<Matrix> layer = new Parallel<>(new Matrix[sizes[0].length]);
		for (int i = 0; i < sizes[0].length; i++) {
			Matrix ai = adaptedImage(layers[0], sizes[0][i]);
			layer.getArray()[i] = ai.multiply(weights[0][i]);
		}
		layer.invoke(TRANSPOSE);
		return null;
	}

	// prevLayer is array of n^2 x 1 (the previous layer)
	private Matrix adaptedImage(Matrix[] layer, int filterSize)
			throws IllegalArgumentException, ReflectiveOperationException {
		final int imgWidth = sqrt(layer[0].rows());
		final int iterations = imgWidth - filterSize + 1;
		final double[][] result = new double[sqr(iterations)][layer.length * sqr(filterSize)];
		int i = 0;
		for (int x = 0; x < iterations; x++) {
			for (int y = 0; y < iterations; y++) {
				int j = 0;
				for (int m = 0; m < layer.length; m++) {
					for (int fx = 0; fx < filterSize; fx++) {
						for (int fy = 0; fy < filterSize; fy++) {
							result[i][j++] = layer[m].get(coordToOrdinal(x + fx, y + fy, imgWidth), 1);
						}
					}
				}
				i++;
			}
		}
		return new Matrix(result);
	}

	private static int sqr(int n) {
		return n * n;
	}

	private static int sqrt(int n) {
		return (int) Math.sqrt(n);
	}

	private static void checkConstructorArgs(int[][] sizes, int[] nodes) {
		if (nodes.length < 1) {
			throw new IllegalArgumentException("Must have at least 1 fully connected layer.");
		}
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] < 1) {
				throw new IllegalArgumentException("Fully connected layers must have at least 1 node.");
			}
		}
		if (sizes.length < 1) {
			throw new IllegalArgumentException("Must have at least 1 convolution layer.");
		}
		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i].length < 1) {
				throw new IllegalArgumentException("Must have at least one filter.");
			}
			for (int j = 0; j < sizes[i].length; j++) {
				if (sizes[i][j] < 3) {
					throw new IllegalArgumentException("Filters must be at least 3x3.");
				}
			}
		}
	}

}
