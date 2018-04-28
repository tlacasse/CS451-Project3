package ttt.learning;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ParallelMatrix {

	public static final Method SCALAR, ADD, ELEMENTSQUARE;

	static {
		SCALAR = get("scalar", double.class);
		ADD = get("add", Matrix.class);
		ELEMENTSQUARE = get("elementSquare");
	}

	private static Method get(String name, Class<?>... parameterTypes) {
		try {
			return Matrix.class.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException | SecurityException nsmese) {
			nsmese.printStackTrace();
		}
		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	private Matrix[] array;

	public ParallelMatrix(Matrix[] array) {
		this.array = array;
	}

	public Matrix[] get() {
		return array;
	}

	public void invoke(Method method, Object... args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (!method.getReturnType().equals(Matrix.class)) {
			throw new IllegalArgumentException("Can call on methods that return matrices.");
		}
		for (int i = 0; i < array.length; i++) {
			array[i] = (Matrix) method.invoke(array[i], args);
		}
	}

	// not used, keeping to save code
	@Deprecated
	@SuppressWarnings("unchecked")
	public <R> R[] aaa(Method method, Object... args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		R[] result = (R[]) Array.newInstance(method.getReturnType(), array.length);
		for (int i = 0; i < array.length; i++) {
			result[i] = (R) method.invoke(array[i], args);
		}
		return result;
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
		ParallelMatrix pm = new ParallelMatrix(arr);
		pm.invoke(ADD, a);
		arr = pm.get();
		arr[0].display();
		arr[1].display();
	}

}
