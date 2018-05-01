package ttt.testing;

import ttt.learning.Matrix;
import ttt.learning.NeuralNetwork;

final class NNTest {

	private NNTest() {
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		// test NN, housing prices based of #bedrooms, #bathroom, sq ft
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
