package ttt.util;

import java.lang.reflect.Method;

import ttt.learning.Matrix;

@Deprecated // Not Used
public final class MatrixMethod {

	private MatrixMethod() {
	}

	public static final Method SCALAR, ADD, ELEMENTSQUARE, PRODUCT, MULTIPLY, NEGATIVE, TRANSPOSE, SIGMOID,
			SIGMOIDPRIME, COLUMNS, ROWS, GET;

	static {
		SCALAR = get("scalar", double.class);
		ADD = get("add", Matrix.class);
		ELEMENTSQUARE = get("elementSquare");
		PRODUCT = get("product", Matrix.class);
		MULTIPLY = get("multiply", Matrix.class);
		NEGATIVE = get("negative");
		TRANSPOSE = get("transpose");
		SIGMOID = get("sigmoid");
		SIGMOIDPRIME = get("sigmoidPrime");
		COLUMNS = get("columns");
		ROWS = get("rows");
		GET = get("get", int.class, int.class);
	}

	private static Method get(String name, Class<?>... parameterTypes) {
		try {
			return Matrix.class.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException | SecurityException nsmese) {
			nsmese.printStackTrace();
		}
		return null;
	}

}
