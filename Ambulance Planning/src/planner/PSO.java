package planner;

import java.util.Arrays;
import java.util.Random;

/**
 * Particle Swarm Optimization algorithm.
 */
public class PSO {

	/**
	 * Abstract class that connects PSO with real task.
	 */
	public static abstract class PSOEvaluator {
		/**
		 * Decode particle and return estimated value.
		 *
		 * @param particle
		 *            encoded particle
		 * @return estimated value
		 */
		public abstract double evaluate(double[] particle);
	}

	/**
	 * Constants for PSO algorithm.
	 * <p>
	 * Settings example: {MAX_ITER = 0; SWARM_SIZE = N} - generate N random
	 * points and simply choose the best one ("turn off PSO").
	 */
	public static class PSOSettings {
		public int SWARM_SIZE = 30;

		public int MAX_ITER = 1000;
		public int GLOBAL_ITER_THRES = 50;

		public double OMEGA = 0.6;
		public double PHI_LOCAL = 0.3;
		public double PHI_GLOBAL = 0.1;
	}

	private static final Random random = new Random(239);

	private PSOSettings settings;
	private int swarmSize;
	private PSOEvaluator evaluator;

	private int particleDims;
	private double[][] worldBounds;

	private double[][] particles;
	private double[][] velocities;
	private double[][] localBest;
	private double[] globalBest;
	private int[] localBestIteration; // todo: use to re-init bad outdated
										// particles (not necessary)
	private int globalBestIteration;

	private double[] localBestEval;

	private double globalBestEval;

	private int iteration;

	public PSO(PSOEvaluator evaluator, int particleDims, double[][] worldBounds) {
		this(evaluator, particleDims, worldBounds, new PSOSettings());
	}

	public PSO(PSOEvaluator evaluator, int particleDims, double[][] worldBounds, PSOSettings settings) {
		assert particleDims == worldBounds.length;

		this.evaluator = evaluator;
		this.particleDims = particleDims;
		this.worldBounds = worldBounds;
		this.settings = settings;
		swarmSize = settings.SWARM_SIZE;

		init();
	}

	/**
	 * Get a valid random particle to use it outside PSO.
	 */
	public double[] generateRandomParticle() {
		double[] p = new double[particleDims];
		for (int j = 0; j < particleDims; j++) {
			double min = worldBounds[j][0];
			double max = worldBounds[j][1];
			p[j] = unid(min, max);
		}
		return p;
	}

	/**
	 * Initialize the swarm of particles.
	 */
	private void init() {
		/*
		 * Initialize all arrays
		 */
		particles = new double[swarmSize][particleDims];
		velocities = new double[swarmSize][particleDims];

		localBest = new double[swarmSize][];
		globalBest = null;

		localBestIteration = new int[swarmSize];
		globalBestIteration = 0;
		iteration = 0;

		localBestEval = new double[swarmSize];
		Arrays.fill(localBestEval, Double.POSITIVE_INFINITY);
		globalBestEval = Double.POSITIVE_INFINITY;

		/*
		 * Randomize initial swarm, in respect of given bounds
		 */
		for (int i = 0; i < swarmSize; i++) {
			for (int j = 0; j < particleDims; j++) {
				double min = worldBounds[j][0];
				double max = worldBounds[j][1];
				particles[i][j] = unid(min, max);
				velocities[i][j] = unid(-(max - min), max - min);
			}
			localBest[i] = Arrays.copyOf(particles[i], particleDims);
			localBestEval[i] = evaluator.evaluate(particles[i]);
			if (localBestEval[i] < globalBestEval) {
				globalBest = Arrays.copyOf(localBest[i], particleDims);
				globalBestEval = localBestEval[i];
			}
		}

		if (globalBest == null)
			throw new IllegalArgumentException("No valid solution found in initial particle swarm");
	}

	/**
	 * Perform one iteration.
	 */
	private void performIteration() {
		shiftParticles();
		updateValues();
	}

	/**
	 * Find and return best solution.
	 *
	 * @param millis
	 *            max time in milliseconds
	 * @return best found particle
	 */
	public double[] run(long millis) {
		long algorithmStartTime = System.currentTimeMillis();
		for (int it = 0; it < settings.MAX_ITER && iteration - globalBestIteration < settings.GLOBAL_ITER_THRES
				&& System.currentTimeMillis() - algorithmStartTime < millis; it++) {
			performIteration();
		}

		return globalBest;
	}

	/**
	 * Shift particles according to their velocities and best solutions.
	 */
	private void shiftParticles() {
		for (int i = 0; i < swarmSize; i++) {
			for (int j = 0; j < particleDims; j++) {
				double rLocal = random.nextDouble();
				double rGlobal = random.nextDouble();
				velocities[i][j] = settings.OMEGA * velocities[i][j]
						+ settings.PHI_LOCAL * rLocal * (localBest[i][j] - particles[i][j])
						+ settings.PHI_GLOBAL * rGlobal * (globalBest[j] - particles[i][j]);
				particles[i][j] += velocities[i][j];
			}
		}
	}

	/**
	 * Uniform distribution U[l; r].
	 *
	 * @param l
	 *            left bound
	 * @param r
	 *            right bound
	 * @return point on the specified segment
	 */
	private double unid(double l, double r) {
		return l + random.nextDouble() * (r - l);
	}

	/**
	 * Update local and global memories.
	 */
	private void updateValues() {
		for (int i = 0; i < swarmSize; i++) {
			double curEval = evaluator.evaluate(particles[i]);
			if (curEval < localBestEval[i]) {
				localBest[i] = Arrays.copyOf(particles[i], particleDims);
				localBestEval[i] = curEval;
				localBestIteration[i] = iteration;
				if (curEval < globalBestEval) {
					globalBest = Arrays.copyOf(particles[i], particleDims);
					globalBestEval = curEval;
					globalBestIteration = iteration;
				}
			}
		}
	}
}
