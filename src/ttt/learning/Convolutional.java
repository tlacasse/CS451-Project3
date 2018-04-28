package ttt.learning;

/**
 * NOT really a convolutional neural network, because I can't figure those out.
 * It is somewhere in between. This may be better due to a game board being more
 * image like.
 */
public class Convolutional {

	private final int[][] sizes;
	private final int[] nodes;
	private Matrix[] weights, layers;

	public Convolutional(int[][] sizes, int... nodes) {
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
		this.sizes = sizes;
		this.nodes = nodes;
		weights = new Matrix[1000];
		for (int i = 0; i < sizes.length; i++) {

		}
	}

	public Matrix calculate(Matrix x) {
		layers[0] = x;
		for (int i = 0; i < sizes[0].length; i++) {

		}
		return null;
	}

	private Matrix adaptedImage(Matrix x) {
		// double[][] m = new double[][];
		return x;
	}

}
