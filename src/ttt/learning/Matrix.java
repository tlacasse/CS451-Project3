package ttt.learning;

import java.util.Arrays;
import java.util.Random;

public class Matrix {

	public static Matrix identity(int size) {
		final double[][] data = new double[size][size];
		for (int i = 0; i < size; i++) {
			data[i][i] = 1.0;
		}
		return new Matrix(data);
	}

	private static final Random RANDOM = new Random();

	public static Matrix random(int rows, int cols) {
		final double[][] data = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = 1 - RANDOM.nextDouble();
			}
		}
		return new Matrix(data);
	}

	/////////////////////////////////////////////////////////////

	private final int rows, cols;

	private final double[][] data;

	/////////////////////////////////////////////////////////////

	public Matrix(int rows, int cols) {
		this(rows, cols, 0.0);
	}

	public Matrix(int rows, int cols, double init) {
		this.rows = rows;
		this.cols = cols;
		this.data = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = init;
			}
		}
	}

	public Matrix(double[][] data) {
		this.rows = data.length;
		this.cols = data[0].length;
		this.data = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] = data[i][j];
			}
		}
	}

	public Matrix(double[] data, int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.data = new double[rows][cols];
		int di = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] = data[di++];
			}
		}
	}

	public Matrix(boolean columnMatrix, double... ds) {
		if (columnMatrix) {
			this.rows = ds.length;
			this.cols = 1;
			this.data = new double[rows][cols];
			for (int i = 0; i < rows; i++) {
				data[i][0] = ds[i];
			}
		} else {
			this.rows = 1;
			this.cols = ds.length;
			this.data = new double[rows][cols];
			for (int i = 0; i < cols; i++) {
				data[0][i] = ds[i];
			}
		}
	}

	/////////////////////////////////////////////////////////////

	public Matrix copy() {
		final double[][] newData = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				newData[i][j] = data[i][j];
			}
		}
		return new Matrix(newData);
	}

	/////////////////////////////////////////////////////////////

	public int[] dimensions() {
		return new int[] { rows, cols };
	}

	public int rows() {
		return rows;
	}

	public int columns() {
		return cols;
	}

	public double get(int row, int col) {
		return data[row][col];
	}

	public void set(int row, int col, double val) {
		data[row][col] = val;
	}

	/////////////////////////////////////////////////////////////

	public Matrix scalar(double s) {
		Matrix m = this.copy();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				m.data[i][j] *= s;
			}
		}
		return m;
	}

	public Matrix add(double a) {
		Matrix m = this.copy();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				m.data[i][j] += a;
			}
		}
		return m;
	}

	public Matrix elementSquare() {
		Matrix m = this.copy();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				m.data[i][j] = data[i][j] * data[i][j];
			}
		}
		return m;
	}

	/////////////////////////////////////////////////////////////

	public Matrix add(Matrix other) {
		Matrix m = this.copy();
		if (this.cols != other.cols || this.rows != other.rows) {
			throw new IllegalArgumentException("Unable to add: dimensions [" + this.rows + "," + this.cols
					+ "] do not match [" + other.rows + "," + other.cols + "]");
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				m.data[i][j] += other.get(i, j);
			}
		}
		return m;
	}

	public Matrix product(Matrix other) {
		Matrix m = this.copy();
		if (this.cols != other.cols || this.rows != other.rows) {
			throw new IllegalArgumentException("Unable to multiply elementwise: dimensions [" + this.rows + ","
					+ this.cols + "] do not match [" + other.rows + "," + other.cols + "]");
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				m.data[i][j] *= other.get(i, j);
			}
		}
		return m;
	}

	public Matrix multiply(Matrix other) {
		if (this.cols != other.rows) {
			throw new IllegalArgumentException(
					"Unable to matrix multiply: " + this.cols + " do not match " + other.rows + " rows");
		}
		Matrix m = new Matrix(this.rows, other.cols);
		for (int j = 0; j < other.cols; j++) {
			for (int i = 0; i < this.rows; i++) {
				for (int k = 0; k < this.cols; k++) {
					m.data[i][j] += this.data[i][k] * other.data[k][j];
				}
			}
		}
		return m;
	}

	/////////////////////////////////////////////////////////////

	public Matrix negative() {
		Matrix m = this.copy();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				m.data[i][j] = -data[i][j];
			}
		}
		return m;
	}

	public Matrix transpose() {
		double[][] t = new double[cols][rows];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				t[j][i] = data[i][j];
			}
		}
		return new Matrix(t);
	}

	public Matrix sigmoid() {
		Matrix m = this.copy();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				m.data[i][j] = sigmoid(data[i][j]);
			}
		}
		return m;
	}

	public Matrix sigmoidPrime() {
		Matrix m = this.copy();
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				m.data[i][j] = sigmoidPrime(data[i][j]);
			}
		}
		return m;
	}

	/////////////////////////////////////////////////////////////

	public double sum() {
		double result = 0.0;
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				result += data[i][j];
			}
		}
		return result;
	}

	public double max() {
		double max = (double) Integer.MIN_VALUE;
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				max = Math.max(max, data[i][j]);
			}
		}
		return max;
	}

	public Matrix normalize() {
		return normalize(this.max());
	}

	public Matrix normalize(double max) {
		Matrix m = this.copy();
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				m.data[i][j] = data[i][j] / max;
			}
		}
		return m;
	}

	// would be useful for a classification problem
	public int[] indexOfMax() {
		int[] pos = new int[] { -1, -1 };
		double max = (double) Integer.MIN_VALUE;
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				if (data[i][j] > max) {
					max = data[i][j];
					pos[0] = i;
					pos[1] = j;
				}
			}
		}
		return pos;
	}

	/////////////////////////////////////////////////////////////

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Matrix other = (Matrix) obj;
		if (cols != other.cols) {
			return false;
		}
		if (rows != other.rows) {
			return false;
		}
		return Arrays.deepEquals(data, other.data);
	}

	@Override
	public String toString() {
		String result = "";
		int maxSize = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				maxSize = Math.max(maxSize, ("" + data[i][j]).length());
			}
		}
		maxSize += 2;
		for (int i = 0; i < rows; i++) {
			result += "[ ";
			for (int j = 0; j < cols; j++) {
				int thisSize = ("" + data[i][j]).length();
				int sb = (int) Math.floor((maxSize - thisSize) / 2);
				int sa = maxSize - thisSize - sb;
				for (int k = 0; k < sb; k++) {
					result += " ";
				}
				result += data[i][j];
				for (int k = 0; k < sa; k++) {
					result += " ";
				}
			}
			result += " ]\n";
		}
		return result;
	}

	public void display() {
		System.out.println(toString());
	}

	/////////////////////////////////////////////////////////////

	private static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}

	private static double sigmoidPrime(double x) {
		double s = sigmoid(x);
		return s * (1 - s);
	}

}
