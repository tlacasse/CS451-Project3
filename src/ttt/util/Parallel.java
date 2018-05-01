package ttt.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * An array of objects, can easily call the same method on all objects in the
 * array.
 */
public final class Parallel<P> {

	private final P[] array;

	public Parallel(P[] array) {
		if (array.length < 1) {
			throw new IllegalArgumentException("Illegal Size: " + array.length);
		}
		this.array = array;
	}

	public int count() {
		return array.length;
	}

	public P[] getArray() {
		return array;
	}

	@SuppressWarnings("unchecked")
	public void invoke(Method method, Object... args) throws IllegalArgumentException, ReflectiveOperationException {
		if (!method.getReturnType().equals(array[0].getClass())) {
			throw new IllegalArgumentException("Can only call on methods that return this Parallel type.");
		}
		for (int i = 0; i < array.length; i++) {
			array[i] = (P) method.invoke(array[i], args);
		}
	}

	@SuppressWarnings("unchecked")
	public <R> List<R> invokeAndReturn(Method method, Object... args)
			throws IllegalArgumentException, ReflectiveOperationException {
		List<R> result = new ArrayList<>(array.length);
		for (int i = 0; i < array.length; i++) {
			result.add((R) method.invoke(array[i], args));
		}
		return result;
	}

}
