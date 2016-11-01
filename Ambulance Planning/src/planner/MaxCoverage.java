package planner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.CityMap;

/**
 * @author Team 14
 *
 */
public class MaxCoverage {
	/**
	 * Classify each node to a cluster based on the shortest distances to the
	 * centroid of the cluster
	 * 
	 * @params distance - the shortest distance matrix
	 * 
	 * @params bestGuess - the centroids for various clusters returns the
	 *         annotated cluster for each node
	 */
	public static int[] annotate(double[][] distance, int[] bestGuess) {
		int numNodes = distance.length;
		int numClusters = bestGuess.length;

		int[] annotatedNodes = new int[numNodes];
		for (int i = 0; i < numNodes; i++) {
			double min = Double.POSITIVE_INFINITY;
			int ind = -1;
			for (int j = 0; j < numClusters; j++) {
				if (distance[i][bestGuess[j]] < min) {
					min = distance[i][bestGuess[j]];
					ind = j;
				}
			}
			annotatedNodes[i] = ind;
		}
		return annotatedNodes;
	}

	/**
	 * Returns the geometric mean of a given sequence of distances
	 */
	public static double calcGM(double[] dist) {
		double GM = 1;
		for (double element : dist) {
			GM *= element;
		}
		GM = Math.pow(GM, 1.0 / dist.length);
		return GM;
	}

	/**
	 * Finds the optimum centroids of given clusters of points
	 * 
	 * @params numAmb - number of available ambulances
	 * 
	 * @params distance - the shortest distance matrix
	 * 
	 * @params demandNorm - the normalised demand at each node
	 * 
	 * @params demand - the demand at each node
	 * 
	 * @params classArr - to which cluster does each node belongs to returns -
	 *         the node numbers where the centroids must be placed
	 */
	public static int[] centroidFinder(int numAmb, double[][] distance, double[] demandNorm, double[] demand,
			int[] classArr) {

		int[] optLocation = new int[numAmb];
		int numNodes = classArr.length;

		for (int i = 0; i < numAmb; i++) {
			List<Integer> classNow = new ArrayList<Integer>();
			for (int j = 0; j < numNodes; j++) {
				if (classArr[j] == i) {
					classNow.add(j);
				}
			} // end of list for

			int lstSize = classNow.size();

			if (lstSize == 1) {
				optLocation[i] = classNow.get(0);
				continue;
			}

			double max = Double.NEGATIVE_INFINITY;
			int bestCentroid = -1;

			for (int x = 0; x < lstSize; x++) {
				int iter = 0;
				double[][] weightDist = new double[lstSize - 1][2];

				for (int y = 0; y < lstSize; y++) {
					if (classNow.get(y) != classNow.get(x)) {
						weightDist[iter][0] = demand[classNow.get(y)];
						weightDist[iter][1] = distance[classNow.get(x)][classNow.get(y)];
						iter++;
					}
				}
				double currConf = penalty(weightDist, demand[classNow.get(x)]);
				// System.out.println("PENALTY = " + currConf);
				if (currConf > max) {
					max = currConf;
					bestCentroid = classNow.get(x);
				}
				// if else condn for min
			}
			optLocation[i] = bestCentroid;
		} // end of main class for

		return optLocation;
	}

	/**
	 * Evaluates the random initializations for clustering the nodes
	 * 
	 * @params init - init nodes
	 * @params distance - the shortest distance matrix
	 */
	public static double evalInit(int[] init, double[][] distance) {
		// evaluates the random initialisations for the k-means
		double[] dist = new double[init.length];
		double eval = 0;

		for (int i = 0; i < init.length; i++) {
			double min = Double.POSITIVE_INFINITY;
			for (int j = 0; j < init.length; j++) {
				if (i != j) {
					if (distance[init[i]][init[j]] < min) {
						min = distance[init[i]][init[j]];
					}
				}
			}
			dist[i] = min;
		}

		eval = calcGM(dist);
		return eval;
	}

	/**
	 * Finds optimal location for the ambulances to be placed
	 * 
	 * @param numCentroid
	 *            the number of available ambulances
	 * @param map
	 *            the city map
	 * @returns the optimal location for the ambulances
	 */
	public static int[] findMaxCoverageLocations(int numCentroid, CityMap map) {
		double[][] distance = map.getShortestDistances();
		double[] demand = map.getDemands().stream().mapToDouble(d -> d).toArray();
		double demandSum = map.getDemands().stream().max(Double::compareTo).get();
		double[] demandNorm = map.getDemands().stream().mapToDouble(d -> d / demandSum).toArray();

		int[] optLocation = new int[numCentroid];
		int numNodes = demand.length;
		if (numCentroid == 1) {
			int[] classArr = new int[demand.length];
			for (int i = 0; i < demand.length; i++) {
				classArr[i] = 0;
			}
			optLocation = centroidFinder(1, distance, demandNorm, demand, classArr);
		} else {
			int randInits = 19;
			int[][] init = new int[randInits][numCentroid];
			Random rnd = new Random();

			double[] initConf = new double[randInits];
			double max = Double.NEGATIVE_INFINITY;
			int ind = -1;
			for (int i = 0; i < randInits; i++) {
				for (int j = 0; j < numCentroid; j++) {
					init[i][j] = rnd.nextInt(numNodes);
				}
				initConf[i] = evalInit(init[i], distance);

				if (initConf[i] > max) {
					ind = i;
					max = initConf[i];
				}
			}
			int[] bestGuess = init[ind];

			int iter = 0;
			int[] annotateNodes = null;
			while (iter < 11) {
				annotateNodes = annotate(distance, bestGuess);
				// if(numAmbulances<numNodes)
				bestGuess = centroidFinder(numCentroid, distance, demandNorm, demand, annotateNodes);
				iter++;
			}

			optLocation = bestGuess;
		}

		return optLocation;
	}

	/**
	 * Calculates the confidence for a node to be an apt location for the
	 * ambulance to be placed in a cluster
	 * 
	 * @params weightDist - weighted distance from other nodes
	 * 
	 * @params demNode - demand at that node
	 */
	public static double penalty(double[][] weightDist, double demNode) {
		double denominator = 0;
		for (double[] element : weightDist) {
			// numerator+=weightDist[i][0];
			denominator += element[1];
		}
		return 1.0 / (denominator / (weightDist.length + 1));
	}
}
