package utils;

import java.util.Arrays;

public class Utils {

	/**
	 * Return squared distance between given points.
	 */
	public static double distSqr(double x1, double y1, double x2, double y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	/**
	 * Get ordered indices of array elements after sorting.
	 * <p>
	 * Example: [0.3, 0.5, 0.2] -> [2, 0, 1].
	 *
	 * @param x
	 *            input array
	 * @return array of indices
	 */
	public static int[] getSortedIndices(double[] x) {
		Pair<Double, Integer>[] pairs = new Pair[x.length];
		for (int i = 0; i < pairs.length; i++) {
			pairs[i] = new Pair<>(x[i], i);
		}
		Arrays.sort(pairs, (p1, p2) -> Double.compare(p1.x, p2.x));
		int[] indices = new int[x.length];
		for (int i = 0; i < pairs.length; i++) {
			indices[i] = pairs[i].y;
		}
		return indices;
	}

	private Utils() {
	}
}
