package ttt.testing;

import static ttt.learning.Parallel.ADD;
import static ttt.learning.Parallel.ROWS;

import java.util.List;

import ttt.learning.Matrix;
import ttt.learning.Parallel;

final class ParallelInvokeTest {

	private ParallelInvokeTest() {
	}

	public static void main(String[] args) throws Exception {
		double[][] ada = new double[3][3];
		ada[0] = new double[] { 4, 4, 7 };
		ada[1] = new double[] { 3, 2, 2 };
		ada[2] = new double[] { 1, 4, 6 };

		double[][] bda = new double[3][3];
		bda[0] = new double[] { 2, 4, 9 };
		bda[1] = new double[] { 7, 4, 77 };
		bda[2] = new double[] { 9, 2, 1 };

		Matrix a = new Matrix(ada);
		Matrix b = new Matrix(bda);

		Matrix[] arr = new Matrix[] { a, b };
		Parallel pm = new Parallel(arr);
		pm.invoke(ADD, a);
		arr = pm.getArray();
		arr[0].display();
		arr[1].display();
		List<Integer> rows = pm.invokeAndReturn(ROWS);
		for (int i = 0; i < rows.size(); i++) {
			System.out.println(rows.get(i));
		}
	}

}
