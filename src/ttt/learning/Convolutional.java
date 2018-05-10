package ttt.learning;

import static ttt.util.TTTUtil.coordToOrdinal;

import java.util.Arrays;

/**
 * Attempted Convolutional Neural Network
 */
public final class Convolutional implements AI {

	private final int[] imgSize, convLayers, fullLayers;
	private final Layer[] layers;
	private Matrix input, output;
	private Matrix[] weights;
	private int on;

	public Convolutional(int initialImageWidth, int[] convLayers, int[] fullLayers) {
		checkConstructorArgs(convLayers, fullLayers);
		this.convLayers = convLayers;
		this.fullLayers = fullLayers;
		imgSize = new int[convLayers() + 1];
		imgSize[0] = initialImageWidth;
		weights = new Matrix[layers()];
		layers = new Layer[layers()];
		on = 0;

		for (int i = 0; i < convLayers(); i++) {
			weights[i] = Matrix.random(1, sqr(convLayers[i]));
			imgSize[i + 1] = postConvWidth(imgSize[i], convLayers[i]);
		}
		weights[convLayers()] = Matrix.random(sqr(imgSize[convLayers()]), fullLayers[0], 0.35);
		for (int i = 0; i < fullLayers() - 1; i++) {
			weights[convLayers() + i + 1] = Matrix.random(fullLayers[i], fullLayers[i + 1], 0.35);
		}

		int i = 0;
		for (; i < convLayers(); i++) {
			layers[i] = new ConvLayer(i, weights[i], i == 0 ? null : layers[i - 1], imgSize[i], convLayers[i]);
		}
		for (; i < layers(); i++) {
			layers[i] = new FullLayer(i, weights[i], layers[i - 1]);
		}
		for (i = 0; i < layers() - 1; i++) {
			layers[i].next = layers[i + 1];
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
		return weights;
	}

	@Override
	public void setWeights(Matrix[] ws) {
		if (ws.length != weights.length) {
			throw new IllegalArgumentException("ws.length != weights.length");
		}
		for (int i = 0; i < weights.length; i++) {
			if (ws[i].rows() != weights[i].rows()) {
				throw new IllegalArgumentException("ws[i].rows() != weights[i].rows()");
			}
			if (ws[i].columns() != weights[i].columns()) {
				throw new IllegalArgumentException("ws[i].columns() != weights[i].columns()");
			}
		}
		weights = ws;
		for (int l = 0; l < weights.length; l++) {
			layers[l].weight = weights[l];
		}
	}

	/////////////////////////////////////////////////////////////

	@Override
	public Matrix calculate(Matrix x) {
		input = x;
		output = layers[0].forward(x);
		return output;
	}

	@Override
	public double cost(Matrix y) {
		return (0.5 * y.add(output().negative()).elementSquare().sum()) / input().rows();
	}

	@Override
	public Matrix[] costPrime(Matrix y) {
		final Matrix[] derivative = new Matrix[layers()];
		for (int i = 0; i < layers(); i++) {
			derivative[i] = layers[layers.length - 1].backward(y, i).scalar(0.001);
		}
		on++;
		return derivative;
	}

	/////////////////////////////////////////////////////////////

	private abstract class Layer {

		final int index;
		Layer prev, next;
		Matrix weight, layer, transition, delta;
		int prevOn;

		Layer(int index, Matrix weight, Layer prev) {
			this.index = index;
			this.weight = weight;
			this.prev = prev;
			next = null;
			layer = transition = delta = null;
			prevOn = -1;
		}

		abstract Matrix forward(Matrix x);

		abstract Matrix backward(Matrix y, int wi, int back);

		final Matrix backward(Matrix y, int wi) {
			return backward(y, wi, 0);
		}

	}

	private class FullLayer extends Layer {

		FullLayer(int index, Matrix weight, Layer prev) {
			super(index, weight, prev);
		}

		@Override
		Matrix forward(Matrix x) {
			layer = x;
			transition = x.multiply(weight);
			final Matrix out = transition.sigmoid();
			return next == null ? out : next.forward(out);
		}

		@Override
		Matrix backward(Matrix y, int wi, int back) {
			delta = prevOn == on ? delta
					: (back == 0 ? output().add(y.negative()).product(transition.sigmoidPrime())
							: y.multiply(next.weight.transpose()).product(transition.sigmoidPrime()));
			prevOn = on;
			return wi == index ? layer.transpose().multiply(delta) : prev.backward(delta, wi, back - 1);
		}

	}

	private class ConvLayer extends Layer {

		int imgWidth, filterWidth, newWidth;

		ConvLayer(int index, Matrix weight, Layer prev, int imgWidth, int filterWidth) {
			super(index, weight, prev);
			this.imgWidth = imgWidth;
			this.filterWidth = filterWidth;
			this.newWidth = postConvWidth(imgWidth, filterWidth);
		}

		@Override
		Matrix forward(Matrix x) {
			layer = x;
			final double[][] result = new double[x.rows()][sqr(newWidth)];
			for (int m = 0; m < x.rows(); m++) {
				for (int xx = 0; xx < newWidth; xx++) {
					for (int yy = 0; yy < newWidth; yy++) {
						double sum = 0.0;
						for (int i = 0; i < filterWidth; i++) {
							for (int j = 0; j < filterWidth; j++) {
								sum += x.get(m, coordToOrdinal(xx + i, yy + j, imgWidth))
										* weight.get(0, coordToOrdinal(i, j, filterWidth));
							}
						}
						result[m][coordToOrdinal(xx, yy, newWidth)] = sum;
					}
				}
			}
			transition = new Matrix(result);
			return next.forward(transition.sigmoid());
		}

		@Override
		Matrix backward(Matrix y, int wi, int back) {
			if (prevOn != on) {
				if (back == -fullLayers()) {
					delta = y.multiply(next.weight.transpose()).product(transition.sigmoidPrime());
				} else {
					final int nextFilter = sqrt(next.weight.columns());
					final int nextImage = sqrt(y.columns());
					final double[][] d = new double[y.rows()][sqr(newWidth)];
					for (int m = 0; m < y.rows(); m++) {
						for (int i = 0; i < nextFilter; i++) {
							for (int j = 0; j < nextFilter; j++) {
								final int pi = coordToOrdinal(i, j, nextFilter);
								for (int xx = 0; xx < nextImage; xx++) {
									for (int yy = 0; yy < nextImage; yy++) {
										final int di = coordToOrdinal(xx + i, yy + j, newWidth);
										final int ni = coordToOrdinal(xx, yy, nextImage);
										d[m][di] += y.get(m, ni) * next.weight.get(0, pi)
												* sigmoidPrime(transition.get(m, di));
									}
								}
							}
						}
					}
					delta = new Matrix(d);
				}
			}
			prevOn = on;
			if (wi != index) {
				return prev.backward(delta, wi, back - 1);
			}
			final double[] derivative = new double[sqr(filterWidth)];
			for (int i = 0; i < filterWidth; i++) {
				for (int j = 0; j < filterWidth; j++) {
					double sum = 0.0;
					for (int m = 0; m < y.rows(); m++) {
						for (int xx = 0; xx < newWidth; xx++) {
							for (int yy = 0; yy < newWidth; yy++) {
								final int ni = coordToOrdinal(xx, yy, newWidth);
								final int pi = coordToOrdinal(xx + i, yy + j, imgWidth);
								sum += delta.get(m, ni) * layer.get(m, pi);
							}
						}
					}
					derivative[coordToOrdinal(i, j, filterWidth)] = sum;
				}
			}
			return new Matrix(false, derivative);
		}

	}

	/////////////////////////////////////////////////////////////

	public int getConvLayerSize(int i) {
		return convLayers[i];
	}

	public int getFullLayerSize(int i) {
		return fullLayers[i];
	}

	public int imageSize() {
		return imgSize[0];
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
		return (new StringBuilder()).append(imgSize[0]).append("_").append(Arrays.toString(convLayers)).append("_")
				.append(Arrays.toString(fullLayers)).toString();
	}

	@Override
	public String fileName() {
		return fileName(imgSize[0], convLayers, fullLayers);
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
