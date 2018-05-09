package ttt.learning;

import static ttt.util.TTTUtil.coordToOrdinal;

import java.util.Arrays;

@Deprecated
public final class Convolutional2 implements AI {

	private final int[] imageWidth, convLayers, fullLayers;
	private Matrix input, output;
	private Matrix[] weight;
	private Matrix[] layer, transition;

	public Convolutional2(int initialImageWidth, int[] convLayers, int[] fullLayers) {
		checkConstructorArgs(convLayers, fullLayers);
		this.convLayers = convLayers;
		this.fullLayers = fullLayers;
		imageWidth = new int[convLayers() + 1];
		imageWidth[0] = initialImageWidth;
		layer = new Matrix[layers() + 1];
		transition = new Matrix[layers()];
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

	@Override
	public Matrix input() {
		return input;
	}

	@Override
	public Matrix output() {
		return output;
	}

	/////////////////////////////////////////////////////////////

	@Override
	public Matrix[] getWeights() {
		return weight;
	}

	@Override
	public void setWeights(Matrix[] ws) {
		// no exception messages, just look in stack trace to get to which line
		if (ws.length != weight.length) {
			throw new IllegalArgumentException();
		}
		for (int i = 0; i < weight.length; i++) {
			if (ws[i].rows() != weight[i].rows()) {
				throw new IllegalArgumentException();
			}
			if (ws[i].columns() != weight[i].columns()) {
				throw new IllegalArgumentException();
			}
		}
		weight = ws;
	}

	/////////////////////////////////////////////////////////////

	@Override
	public Matrix calculate(Matrix x) {
		if (x.columns() != sqr(imageWidth[0])) {
			throw new IllegalArgumentException("Wrong image size.");
		}
		input = (layer[0] = x);
		for (int i = 0; i < convLayers(); i++) {
			convolution(i);
		}
		for (int i = 0; i < fullLayers(); i++) {
			transition[fullIndex(i)] = layer[fullIndex(i)].multiply(weight[fullIndex(i)]);
			layer[fullIndex(i + 1)] = transition[fullIndex(i)].sigmoid();
		}
		output = layer[layer.length - 1];
		return output;
	}

	private void convolution(int on) {
		final int imgWidth = imageWidth[convIndex(on)];
		final int filterSize = convLayers[convIndex(on)];
		final int newWidth = postConvWidth(imgWidth, filterSize);
		final double[][] result = new double[layer[convIndex(on)].rows()][sqr(newWidth)];
		for (int m = 0; m < layer[convIndex(on)].rows(); m++) {
			for (int x = 0; x < newWidth; x++) {
				for (int y = 0; y < newWidth; y++) {
					double sum = 0.0;
					for (int i = 0; i < filterSize; i++) {
						for (int j = 0; j < filterSize; j++) {
							sum += layer[convIndex(on)].get(m, coordToOrdinal(x + i, y + j, imgWidth))
									* weight[convIndex(on)].get(0, coordToOrdinal(i, j, filterSize));
						}
					}
					result[m][coordToOrdinal(x, y, newWidth)] = sum;
				}
			}
		}
		transition[convIndex(on)] = new Matrix(result);
		layer[convIndex(on + 1)] = transition[convIndex(on)].sigmoid();
	}

	@Override
	public double cost(Matrix y) {
		return (0.5 * y.add(output().negative()).elementSquare().sum()) / input().rows();
	}

	@Override
	public Matrix[] costPrime(Matrix y) {
		Matrix q;
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
		for (int i = 0; i < layer.length; i++) {
			q = layer[i];
			System.out.println(q.rows() + " x " + q.columns());
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
		for (int i = 0; i < weight.length; i++) {
			q = weight[i];
			System.out.println(q.rows() + " x " + q.columns());
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
		for (int i = 0; i < transition.length; i++) {
			q = transition[i];
			System.out.println(q.rows() + " x " + q.columns());
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");

		final Matrix[] derivative = new Matrix[layers()];
		final int cases = input().rows();
		final int yCount = output().columns();
		if (true) {
			final Matrix[] delta = new Matrix[layers()];
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			// delta_(n-1) = (out - y) prod f'(trans_(n-1))
			q = output().add(y.negative());
			System.out.println(q.rows() + " x " + q.columns());
			q = transition[fullIndex(fullLayers() - 1)].sigmoidPrime();
			System.out.println(q.rows() + " x " + q.columns());
			delta[fullIndex(fullLayers() - 1)] = output().add(y.negative())
					.product(transition[fullIndex(fullLayers() - 1)].sigmoidPrime());
			for (int i = fullLayers() - 2; i >= 0; i--) {
				// delta_i = [ delta_(i+1) mult weight_(i+1)] prod f'(rans_i)
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				q = delta[fullIndex(i + 1)];
				System.out.println(q.rows() + " x " + q.columns());
				q = weight[fullIndex(i + 1)].transpose();
				System.out.println(q.rows() + " x " + q.columns());
				q = transition[fullIndex(i)].sigmoidPrime();
				System.out.println(q.rows() + " x " + q.columns());
				delta[fullIndex(i)] = delta[fullIndex(i + 1)].multiply(weight[fullIndex(i + 1)].transpose())
						.product(transition[fullIndex(i)].sigmoidPrime());
				System.out.println(fullIndex(i));
			}
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			for (int i = 0; i < fullLayers(); i++) {
				// dC/cWi = layer_i^T mult delta_i
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				q = transition[fullIndex(i)].transpose();
				System.out.println(q.rows() + " x " + q.columns());
				q = delta[fullIndex(i)];
				System.out.println(q.rows() + " x " + q.columns());
				derivative[fullIndex(i)] = transition[fullIndex(i)].transpose().multiply(delta[fullIndex(i)]);
			}
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		}
		for (int m = 0; m < 1; m++) {
			final double[][][] delta = new double[layers()][][];

			final int lfi = lastFullIndex();
			delta[lfi] = new double[yCount][1];
			for (int i = 0; i < yCount; i++) {
				delta[lfi][i][0] = (layer[lfi + 1].get(m, i) - input().get(m, i))
						* sigmoidPrime(transition[lfi].get(m, i));
			}
			for (int l = fullLayers() - 2; l >= 0; l--) {
				System.out.println("!!!!\t" + l);
				final int firstLen = getFullLayerSize(l);
				final int secondLen = l > 0 ? getFullLayerSize(l - 1) : sqr(imageWidth[convLayers()]);
				System.out.println(firstLen);
				System.out.println(secondLen);
				delta[fullIndex(l)] = new double[firstLen][secondLen];
				for (int i = 0; i < firstLen; i++) {
					for (int j = 0; j < secondLen; j++) {
						delta[fullIndex(l)][i][j] = weight[fullIndex(l)].get(j, i)
								* sigmoidPrime(transition[fullIndex(l)].get(m, j));
					}
				}
			}
			for (int l = 0; l < convLayers(); l++) {

			}
		}
		return derivative;
	}

	/////////////////////////////////////////////////////////////

	public int getConvLayerSize(int i) {
		return convLayers[i];
	}

	public int getFullLayerSize(int i) {
		return fullLayers[i];
	}

	public int imageSize() {
		return imageWidth[0];
	}

	public int convLayers() {
		return convLayers.length;
	}

	public int fullLayers() {
		return fullLayers.length;
	}

	public int layers() {
		return convLayers() + fullLayers();
	}

	/////////////////////////////////////////////////////////////

	private int convIndex(int i) {
		return i;
	}

	private int fullIndex(int i) {
		return convLayers() + i;
	}

	private int lastFullIndex() {
		return fullIndex(fullLayers() - 1);
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

	private static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}

	private static double sigmoidPrime(double x) {
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

	/////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return (new StringBuilder()).append(imageWidth[0]).append("_").append(Arrays.toString(convLayers)).append("_")
				.append(Arrays.toString(fullLayers)).toString();
	}

	@Override
	public String fileName() {
		return fileName(imageWidth[0], convLayers, fullLayers);
	}

	public static String fileName(int initialImageWidth, int[] convLayers, int[] fullLayers) {
		final StringBuilder sb = new StringBuilder("cnn_");
		sb.append(initialImageWidth).append("_");
		for (int i = 0; i < convLayers.length - 1; i++) {
			sb.append(convLayers[i]).append("-");
		}
		sb.append(convLayers[convLayers.length - 1]).append("_");
		for (int i = 0; i < fullLayers.length - 1; i++) {
			sb.append(fullLayers[i]).append("-");
		}
		sb.append(fullLayers[fullLayers.length - 1]).append(".cnn");
		return sb.toString();
	}

}
