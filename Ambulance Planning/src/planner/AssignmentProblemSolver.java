package planner;

import java.util.Arrays;

/**
 * Solver for an assignment problem. Here we use a version of Hungarian
 * algorithm with asymptotic <code>O(N^3)</code>. Algorithm author: Andrei
 * Lopatin, 2008.
 */
public class AssignmentProblemSolver {

	/**
	 * Should be greater than any number in the given matrix.
	 */
	private static final int INF = Integer.MAX_VALUE / 10;

	//  just testing
	public static void main(String[] args) {
		int[][] a = { { 9, 5, 5, 6, 5 }, { 1, 9, 4, 7, 3 }, { 1, 2, 7, 4, 9 }, { 8, 1, 4, 4, 4 }, { 1, 6, 4, 9, 4 } };
		// SOLUTION a
		// { , ,5, , },
		// { , , , ,3},
		// { , , ,4, },
		// { ,1, , , },
		// {1, , , , }

		System.out.println(Arrays.toString(solve(a)));
	}

	/**
	 * Solving method.
	 * <p>
	 * Note: works for integer values, doubles should be rounded to int before
	 * using the algorithm. todo: comment and get a better understanding
	 *
	 * @param a
	 *            matrix of size NxM (values should be non-negative)
	 * @return array of size N
	 */
	public static int[] solve(int[][] a) {
		int n = a.length;
		if (n == 0)
			return new int[0];
		int m = a[0].length;
		int[] u = new int[n + 1]; // potential
		int[] v = new int[m + 1]; // potential
		int[] p = new int[m + 1]; // maximum matching: for ith row, p[i] -
									// matching column
		int[] way = new int[m + 1]; // way[j] = argmin_i {a[i][j] - u[i] - v[j]}

		for (int i = 1; i <= n; i++) {
			p[0] = i;
			int j0 = 0;
			int[] minv = new int[m + 1]; // minv[j] = min_i {a[i][j] - u[i] -
											// v[j]}
			Arrays.fill(minv, INF);
			boolean[] used = new boolean[m + 1];
			do {
				used[j0] = true;
				int i0 = p[j0];
				int delta = INF;
				int j1 = -1;
				for (int j = 1; j <= m; j++) {
					if (!used[j]) {
						int cur = a[i0 - 1][j - 1] - u[i0] - v[j];
						if (cur < minv[j]) {
							minv[j] = cur;
							way[j] = j0;
						}
						if (minv[j] < delta) {
							delta = minv[j];
							j1 = j;
						}
					}
				}
				for (int j = 0; j <= m; j++) {
					if (used[j]) {
						u[p[j]] += delta;
						v[j] -= delta;
					} else {
						minv[j] -= delta;
					}
				}
				j0 = j1;
			} while (p[j0] != 0);
			do {
				int j1 = way[j0];
				p[j0] = p[j1];
				j0 = j1;
			} while (j0 != 0);
		}

		int[] ans = new int[n];
		for (int j = 1; j <= m; j++) {
			if (p[j] == 0) {
				continue;
			}
			ans[p[j] - 1] = j - 1;
		}

		return ans;
	}
}
