package ttt.learning;

import static ttt.Program.coordToOrdinal;

/**
 * NOT really a convolutional neural network, because I can't figure those out.
 * It is somewhere in between. This may be better due to a game board being more
 * image like.
 */
public class Convolutional {

	private final int size;
	private final int[] imageWidth, convLayers, fullLayers;
	private Matrix[] layer, weights, transition;

	public Convolutional(int initialImageWidth, int[] convLayers, int[] fullLayers) {
		checkConstructorArgs(convLayers, fullLayers);
		this.convLayers = convLayers;
		this.fullLayers = fullLayers;
		size = 1 + convLayers.length + fullLayers.length;
		imageWidth = new int[convLayers.length + 1];
		imageWidth[0] = initialImageWidth;
		layer = new Matrix[size];
		transition = new Matrix[size - 1];

		weights = new Matrix[size - 1];
		for (int i = 0; i < convLayers.length; i++) {
			weights[i] = Matrix.random(sqr(convLayers[i]), 1);
			imageWidth[i + 1] = postConvWidth(imageWidth[i], convLayers[i]);
		}
		weights[convLayers.length] = Matrix.random(sqr(imageWidth[convLayers.length]), fullLayers[0]);
		for (int i = 0; i < fullLayers.length - 1; i++) {
			weights[convLayers.length + 1 + i] = Matrix.random(fullLayers[i], fullLayers[i + 1]);
		}
	}

	public Matrix input() {
		return layer[0];
	}

	public Matrix output() {
		return layer[size - 1];
	}

	public Matrix calculate(Matrix x) {
		layer[0] = x;
		int i = 0;
		for (; i < convLayers.length; i++) {
			final Matrix[] after = convolution(layer[i], weights[i]);
			transition[i] = after[0];
			layer[i + 1] = after[1];
		}
		for (; i < size - 1; i++) {
			transition[i] = layer[i].multiply(weights[i]);
			layer[i + 1] = transition[i].sigmoid();
		}
		return layer[size - 1];
	}

	private Matrix[] convolution(Matrix layer, Matrix filter) {
		final int imgWidth = sqrt(layer.columns());
		final int filterSize = sqrt(filter.rows());
		final int testCases = layer.rows();
		final int iterations = postConvWidth(imgWidth, filterSize);
		final double[][] transition = new double[testCases][sqr(iterations)];
		final double[][] activation = new double[testCases][sqr(iterations)];
		for (int t = 0; t < testCases; t++) {
			for (int x = 0; x < iterations; x++) {
				for (int y = 0; y < iterations; y++) {
					double value = 0;
					for (int fx = 0; fx < filterSize; fx++) {
						for (int fy = 0; fy < filterSize; fy++) {
							final int oldIndex = coordToOrdinal(x + fx, y + fy, imgWidth);
							final int filterIndex = coordToOrdinal(fx, fy, filterSize);
							value += layer.get(t, oldIndex) * filter.get(filterIndex, 0);
						}
					}
					final int newIndex = coordToOrdinal(x, y, iterations);
					transition[t][newIndex] = value;
					activation[t][newIndex] = sigmoid(value);
				}
			}
		}
		return new Matrix[] { new Matrix(transition), new Matrix(activation) };
	}

	private int postConvWidth(int imgSize, int filterSize) {
		return imgSize - filterSize + 1;
	}

	private static final int sqr(int n) {
		return n * n;
	}

	private static final int sqrt(int n) {
		return (int) Math.sqrt(n);
	}

	private static final double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}

	private static final double sigmoidPrime(double x) {
		double s = sigmoid(x);
		return s * (1 - s);
	}

	private static final void checkConstructorArgs(int[] convLayers, int[] fullLayers) {
		if (convLayers.length < 1) {
			throw new IllegalArgumentException("Must have at least 1 convolution layer.");
		}
		for (int i = 0; i < convLayers.length; i++) {
			if (convLayers[i] < 1) {
				throw new IllegalArgumentException("Filters must be bigger than 2x2.");
			}
		}
		if (fullLayers.length < 1) {
			throw new IllegalArgumentException("Must have at least 1 fully connected layer.");
		}
		for (int i = 0; i < fullLayers.length; i++) {
			if (fullLayers[i] < 1) {
				throw new IllegalArgumentException("Layer must have at least 1 node.");
			}
		}
	}

}
