package ttt.learning;

public class NeuralNetwork {

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
		nodes = is.clone();
		size = nodes.length;
		weights = new Matrix[size - 1];
		for (int i = 0; i < is.length - 1; i++) {
			weights[i] = Matrix.random(nodes[i], nodes[i + 1]);
		}
		layer = new Matrix[size];
		transition = new Matrix[size - 1];
	}

	/////////////////////////////////////////////////////////////

	public Matrix input() {
		return layer[0];
	}

	public Matrix output() {
		return layer[size - 1];
	}

	/////////////////////////////////////////////////////////////

	public Matrix calculate(Matrix x) {
		layer[0] = x;
		for (int i = 0; i < size - 1; i++) {
			transition[i] = layer[i].multiply(weights[i]);
			layer[i + 1] = transition[i].sigmoid();
		}
		return layer[size - 1];
	}

	private static final double TUNING = 0.000001;

	private double cost(Matrix y) {
		final double cost = 0.5 * y.add(output().negative()).elementSquare().sum();
		double change = 0.0;
		for (int i = 0; i < size - 1; i++) {
			change += weights[i].elementSquare().sum();
		}
		change *= TUNING * 0.5;
		return (cost / layer[0].rows()) + change;
	}

	private Matrix[] costPrime(Matrix y) {
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

	/////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		// test ANN, housing prices based of #bedrooms, #bathroom, sq ft
		// not much data, is overfitting, just an example though.

		double[][] xda = new double[9][3];
		double[][] yda = new double[9][1];
		int i = 0;
		xda[i] = new double[] { 3, 2, 1328 / 1000 };
		yda[i++] = new double[] { 209900 };
		xda[i] = new double[] { 4, 3, 2830 / 1000 };
		yda[i++] = new double[] { 799900 };
		xda[i] = new double[] { 4, 2, 2380 / 1000 };
		yda[i++] = new double[] { 359900 };
		xda[i] = new double[] { 5, 6, 5707 / 1000 };
		yda[i++] = new double[] { 1900000 };
		xda[i] = new double[] { 4, 4, 3052 / 1000 };
		yda[i++] = new double[] { 924900 };
		xda[i] = new double[] { 5, 5, 5214 / 1000 };
		yda[i++] = new double[] { 1599000 };
		xda[i] = new double[] { 4, 3, 3532 / 1000 };
		yda[i++] = new double[] { 995000 };
		xda[i] = new double[] { 4, 2, 2069 / 1000 };
		yda[i++] = new double[] { 429900 };
		xda[i] = new double[] { 4, 5, 3518 / 1000 };
		yda[i++] = new double[] { 949900 };

		double[][] tda = new double[3][3];
		double[][] tyda = new double[3][1];
		tda[0] = new double[] { 4, 4, 5702 / 1000 }; // 1299000
		tda[1] = new double[] { 3, 2, 2144 / 1000 }; // 529000
		tda[2] = new double[] { 4, 4, 4626 / 1000 }; // 824000
		tyda[0] = new double[] { 1299000 };
		tyda[1] = new double[] { 529000 };
		tyda[2] = new double[] { 824000 };

		Matrix x = (new Matrix(xda)).normalize();
		Matrix y = (new Matrix(yda)).normalize(2000000);
		Matrix t = (new Matrix(tda)).normalize();
		Matrix yt = (new Matrix(tyda)).normalize(2000000);

		NeuralNetwork nn = new NeuralNetwork(3, 3, 2, 1);

		nn.train(x, y);

		nn.calculate(x).scalar(2000000).display();

		nn.calculate(t).scalar(2000000).display();

		System.out.println("Testing Cost: " + nn.cost(yt));
	}

}
