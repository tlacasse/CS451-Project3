package ttt.learning;

import java.util.Arrays;

/**
 * A NeuralNetwork implementation, with cost function and gradient descent for
 * learning. (Hopefully works!)
 */
public final class NeuralNetwork implements AI {

	private final int size;
	private final int[] nodes;
	private Matrix[] weights, layer, transition;

	/////////////////////////////////////////////////////////////

	public NeuralNetwork(int... is) {
		if (is.length < 3) {
			throw new IllegalArgumentException("Must have at least 3 layers.");
		}
		for (int i = 0; i < is.length; i++) {
			if (is[i] < 1) {
				throw new IllegalArgumentException("Layers must have at least 1 node.");
			}
		}
		nodes = is;
		size = nodes.length;
		weights = new Matrix[size - 1];
		for (int i = 0; i < is.length - 1; i++) {
			weights[i] = Matrix.random(nodes[i], nodes[i + 1]);
		}
		layer = new Matrix[size];
		transition = new Matrix[size - 1];
	}

	/////////////////////////////////////////////////////////////

	@Override
	public Matrix input() {
		return layer[0];
	}

	@Override
	public Matrix output() {
		return layer[size - 1];
	}

	/////////////////////////////////////////////////////////////

	// these do not copy the matrix array

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
	}

	/////////////////////////////////////////////////////////////

	@Override
	public Matrix calculate(Matrix x) {
		layer[0] = x;
		for (int i = 0; i < size - 1; i++) {
			transition[i] = layer[i].multiply(weights[i]);
			layer[i + 1] = transition[i].sigmoid();
		}
		return layer[size - 1];
	}

	private static final double TUNING = 0.000001;

	@Override
	public double cost(Matrix y) {
		return (0.5 * y.add(output().negative()).elementSquare().sum()) / layer[0].rows();
	}

	@Override
	public Matrix[] costPrime(Matrix y) {
		final Matrix[] derivative = new Matrix[size - 1];
		final Matrix[] delta = new Matrix[size - 1];
		delta[size - 2] = output().add(y.negative()).product(transition[size - 2].sigmoidPrime());
		for (int i = size - 3; i >= 0; i--) {
			delta[i] = delta[i + 1].multiply(weights[i + 1].transpose()).product(transition[i].sigmoidPrime());
		}
		for (int i = 0; i < size - 1; i++) {
			derivative[i] = layer[i].transpose().multiply(delta[i]).add(weights[i].scalar(TUNING));
		}
		return derivative;
	}

	/////////////////////////////////////////////////////////////

	public int getNodeCount(int layer) {
		return nodes[layer];
	}

	@Override
	public String toString() {
		return Arrays.toString(nodes);
	}

	@Override
	public String fileName() {
		return fileName(nodes);
	}

	public static String fileName(int... nodes) {
		final StringBuilder sb = new StringBuilder("nn_");
		for (int i = 0; i < nodes.length - 1; i++) {
			sb.append(nodes[i]).append("-");
		}
		sb.append(nodes[nodes.length - 1]).append(".nn");
		return sb.toString();
	}

	/////////////////////////////////////////////////////////////

	@Deprecated
	public void train(Matrix x, Matrix y) {
		for (int t = 0; t < 100000; t++) {
			calculate(x);
			if (t % 10000 == 0) {
				System.out.println("Cost at " + t + ": " + cost(y));
			}
			Matrix[] derivative = costPrime(y);
			for (int w = 0; w < weights.length; w++) {
				weights[w] = weights[w].add(derivative[w].negative());
			}
		}
		System.out.println("Training Cost: " + cost(y) + "\n");
	}

}
