package ttt.learning;

import static ttt.Program.coordToOrdinal;

/**
 * NOT really a convolutional neural network, because I can't figure those out.
 * It is somewhere in between. This may be better due to a game board being more
 * image like.
 */
public class Convolutional {

	private final int[] imageWidth, convLayers, fullLayers;
	private Matrix input, output;
	private Matrix[] fullLayer, fullTrans, weight;
	private Matrix[][] convLayer, convTrans, convEditLayer;

	public Convolutional(int initialImageWidth, int[] convLayers, int[] fullLayers) {
		checkConstructorArgs(convLayers, fullLayers);
		this.convLayers = convLayers;
		this.fullLayers = fullLayers;
		imageWidth = new int[convLayers() + 1];
		imageWidth[0] = initialImageWidth;
		fullLayer = new Matrix[1 + fullLayers()];
		fullTrans = new Matrix[fullLayers()];
		convLayer = new Matrix[1 + convLayers()][];
		convTrans = new Matrix[convLayers()][];
		convEditLayer = new Matrix[convLayers()][];

		weight = new Matrix[layers()];
		for (int i = 0; i < convLayers(); i++) {
			weight[convIndex(i)] = Matrix.random(1, sqr(convLayers[i]));
			imageWidth[i + 1] = postConvWidth(imageWidth[i], convLayers[i]);
		}
		weight[convLayers.length] = Matrix.random(sqr(imageWidth[convLayers()]), fullLayers[0], 0.35);
		for (int i = 0; i < fullLayers() - 1; i++) {
			weight[fullIndex(i) + 1] = Matrix.random(fullLayers[i], fullLayers[i + 1], 0.35);
		}
	}

	/////////////////////////////////////////////////////////////

	public Matrix input() {
		return input;
	}

	public Matrix output() {
		return output;
	}

	/////////////////////////////////////////////////////////////

	public Matrix calculate(Matrix x) {
		if (x.columns() != sqr(imageWidth[0])) {
			throw new IllegalArgumentException("Wrong image size.");
		}
		input = x;
		convLayer[0] = rowsToArray(x);
		for (int i = 0; i < convLayers(); i++) {
			convLayer[i + 1] = convolution(convLayer[i], weight[convIndex(i)], i);
		}
		fullLayer[0] = joinRows(convLayer[convLayer.length - 1]);
		for (int i = 0; i < fullLayers(); i++) {
			fullTrans[i] = fullLayer[i].multiply(weight[fullIndex(i)]);
			fullLayer[i + 1] = fullTrans[i].sigmoid();
		}
		output = fullLayer[fullLayer.length - 1];
		return output;
	}

	private Matrix[] convolution(Matrix[] layer, Matrix filter, int transIndex) {
		final int imgWidth = sqrt(layer[0].columns());
		final int filterSize = sqrt(filter.columns());
		final int iterations = postConvWidth(imgWidth, filterSize);
		final Matrix[] result = new Matrix[layer.length];
		convTrans[transIndex] = new Matrix[layer.length];
		convEditLayer[transIndex] = new Matrix[layer.length];
		for (int m = 0; m < layer.length; m++) {
			final double[][] data = new double[filter.columns()][sqr(iterations)];
			for (int x = 0; x < iterations; x++) {
				for (int y = 0; y < iterations; y++) {
					for (int fx = 0; fx < filterSize; fx++) {
						for (int fy = 0; fy < filterSize; fy++) {
							final int filterIndex = coordToOrdinal(fx, fy, filterSize);
							final int newIndex = coordToOrdinal(x, y, iterations);
							final int oldIndex = coordToOrdinal(x + fx, y + fy, imgWidth);
							data[filterIndex][newIndex] = layer[m].get(0, oldIndex);
						}
					}
				}
			}
			convEditLayer[transIndex][m] = new Matrix(data);
			convTrans[transIndex][m] = filter.multiply(convEditLayer[transIndex][m]);
			result[m] = convTrans[transIndex][m].sigmoid();
		}
		return result;
	}

	/////////////////////////////////////////////////////////////

	public double cost(Matrix y) {
		return (0.5 * y.add(output().negative()).elementSquare().sum()) / input.rows();
	}

	public Matrix[] costPrime(Matrix y) {
		final Matrix[] derivative = new Matrix[layers()];
		final int cases = input().rows();

		final Matrix[] delta = new Matrix[fullLayers()];
		// delta_(n-1) = (out - y) prod f'(trans_(n-1))
		delta[fullLayers() - 1] = output().add(y.negative()).product(fullTrans[fullLayers() - 1].sigmoidPrime());
		for (int i = fullTrans.length - 2; i >= 0; i--) {
			// delta_i = [ delta_(i+1) mult weight_(i+1)] prod f'(rans_i)
			delta[i] = delta[i + 1].multiply(weight[fullIndex(i + 1)].transpose()).product(fullTrans[i].sigmoidPrime());
		}
		for (int i = 0; i < fullTrans.length; i++) {
			// dC/cWi = layer_i^T mult delta_i
			derivative[fullIndex(i)] = fullLayer[i].transpose().multiply(delta[i]);
		}

		final Matrix[] minFullDelta = rowsToArray(delta[0]);
		final Matrix[][] cDelta = new Matrix[convLayers()][cases];
		final Matrix[][] cDeriv = new Matrix[convLayers()][cases];
		for (int c = 0; c < cases; c++) {
			// delta_(n-1)
			// = [ delta_full0 mult weight_full0^T ] prod f'(trans_(n-1))
			cDelta[convLayers() - 1][c] = minFullDelta[c].multiply(weight[convLayers()].transpose())
					.product(convTrans[convLayers() - 1][c].sigmoidPrime());
			for (int i = convLayers() - 2; i >= 0; i--) {
				// delta_i
				// = delta_(i+1) mult [ weight_(i+1)^T mult f'(trans_(i)) ]
				cDelta[convIndex(i)][c] = cDelta[convIndex(i) + 1][c].multiply(
						weight[convIndex(i) + 1].transpose().multiply(convTrans[convIndex(i)][c].sigmoidPrime()));
			}
			for (int i = 0; i < convLayers(); i++) {
				// dC/cWi = delta_i mult layer_i^T
				cDeriv[convIndex(i)][c] = cDelta[i][c].multiply(convEditLayer[i][c].transpose());
			}
		}
		for (int i = 0; i < convLayers(); i++) {
			// add cases together
			Matrix sum = cDeriv[convIndex(i)][0];
			for (int c = 1; c < cases; c++) {
				sum = sum.add(cDeriv[convIndex(i)][c]);
			}
			derivative[convIndex(i)] = sum;
		}
		return derivative;
	}

	/////////////////////////////////////////////////////////////

	private int convLayers() {
		return convLayers.length;
	}

	private int fullLayers() {
		return fullLayers.length;
	}

	private int layers() {
		return convLayers() + fullLayers();
	}

	private int convIndex(int i) {
		return i;
	}

	private int fullIndex(int i) {
		return convLayers() + i;
	}

	private static Matrix[] rowsToArray(Matrix m) {
		final Matrix[] result = new Matrix[m.rows()];
		for (int i = 0; i < m.rows(); i++) {
			final double[] data = new double[m.columns()];
			for (int j = 0; j < m.columns(); j++) {
				data[j] = m.get(i, j);
			}
			result[i] = new Matrix(false, data);
		}
		return result;
	}

	private static Matrix joinRows(Matrix[] m) {
		final double[][] data = new double[m.length][m[0].columns()];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].columns(); j++) {
				data[i][j] = m[i].get(0, j);
			}
		}
		return new Matrix(data);
	}

	private static int postConvWidth(int imgSize, int filterSize) {
		return imgSize - filterSize + 1;
	}

	private static final int sqr(int n) {
		return n * n;
	}

	private static final int sqrt(int n) {
		return (int) Math.sqrt(n);
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
