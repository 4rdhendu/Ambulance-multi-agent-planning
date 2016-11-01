package planner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import model.Action;
import model.ActionDrop;
import model.ActionMove;
import model.ActionPick;
import model.Ambulance;
import model.CityMap;
import model.Hospital;
import model.NodeContent;
import model.Patient;
import utils.Pair;
import utils.Utils;

/**
 * Planner that uses PSO as a tool to find better solution.
 */
public class PSOPlanner extends Planner {

	/**
	 * Representation of a plan that is used in algorithm.
	 */
	private class Plan {

		private List<Integer>[] routes;
		private double planCost;

		public Plan(int ambCnt) {
			routes = new List[ambCnt];
			for (int i = 0; i < ambCnt; i++) {
				routes[i] = new ArrayList<>();
			}

			planCost = 0;
		}

		/**
		 * Transform plan from inner representation into real one.
		 *
		 * @return plan representation used by model
		 */
		private Map<Ambulance, List<Action>> toMainRepresentation() {

			System.out.println(toString()); // todo: remove debug

			Map<Ambulance, List<Action>> bigplan = new HashMap<>();

			for (int ambIdx = 0; ambIdx < ambCnt; ambIdx++) {
				Ambulance amb = ambulances.get(ambIdx);
				List<Action> actions = new ArrayList<>();
				/*
				 * Do not forget to add route to the closest hospital if already
				 * have a patient.
				 */
				if (!amb.isFree()) {
					Hospital hos = closestHospital(amb.getNode());
					insertMoveActions(actions, amb, amb.getNode(), hos.getNode());
					actions.add(new ActionDrop(amb, hos.getNode(), amb.getPatient()));
				}

				if (routes[ambIdx].size() == 0) {
					continue;
				}
				// Pick first patient
				int patIdx = routes[ambIdx].get(0);
				Patient pat = patients.get(patIdx);
				insertMoveActions(actions, amb, ambLocations.get(ambIdx), pat.getNode());
				actions.add(new ActionPick(amb, pat.getNode(), pat));

				for (int j = 0; j < routes[ambIdx].size() - 1; j++) {
					int nxtPatIdx = routes[ambIdx].get(j + 1);

					// Drop current patient in an optimal hospital
					Hospital hos = hospitals.get(optHospitals[patIdx][nxtPatIdx]);
					insertMoveActions(actions, amb, pat.getNode(), hos.getNode());
					actions.add(new ActionDrop(amb, hos.getNode(), pat));

					// Pick next patient
					patIdx = nxtPatIdx;
					pat = patients.get(patIdx);
					insertMoveActions(actions, amb, hos.getNode(), pat.getNode());
					actions.add(new ActionPick(amb, pat.getNode(), pat));
				}

				// Drop last patient
				Hospital hos = hospitals.get(singleOptHospitals[patIdx]);
				insertMoveActions(actions, amb, pat.getNode(), hos.getNode());
				actions.add(new ActionDrop(amb, hos.getNode(), pat));

				// Add to the whole plan
				bigplan.put(amb, actions);
			}

			return bigplan;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("Plan cost: ").append(planCost).append("\n");
			for (int i = 0; i < routes.length; i++) {
				int id = ambulances.get(i).getId();
				str.append("A").append(id).append(" ->");
				if (!ambulances.get(i).isFree()) {
					Hospital hos = closestHospital(ambulances.get(i).getNode());
					str.append(" H").append(hos.getId());
				}
				if (routes[i].size() == 0) {
					str.append("\n");
					continue;
				}
				for (int j = 0; j < routes[i].size() - 1; j++) {
					int cur = routes[i].get(j);
					int nxt = routes[i].get(j + 1);
					int hos = optHospitals[cur][nxt];
					str.append(" P").append(patients.get(cur).getId()).append(" H").append(hospitals.get(hos).getId());
				}
				int cur = routes[i].get(routes[i].size() - 1);
				int hos = singleOptHospitals[cur];
				str.append(" P").append(patients.get(cur).getId()).append(" H").append(hospitals.get(hos).getId());
				str.append("\n");
			}

			return str.toString();
		}
	}

	/**
	 * Implementation of PSO Evaluator for VRP problem.
	 */
	public class VRPEvaluator extends PSO.PSOEvaluator {

		@Override
		public double evaluate(double[] particle) {
			Plan plan = decodePlan(particle);
			return evaluatePlan(plan);
		}
	}

	public static void main(String[] args) {

		double[][] adjMatrix = { { 0, 2, 0, 0, 0, 0, 0 }, { 2, 0, 2, 2, 9, 0, 0 }, { 0, 2, 0, 3, 0, 0, 0 },
				{ 0, 2, 3, 0, 0, 0, 0 }, { 0, 9, 0, 0, 0, 7, 1 }, { 0, 0, 0, 0, 7, 0, 0 }, { 0, 0, 0, 0, 1, 0, 0 } };
		double[] demands = new double[7];
		Arrays.fill(demands, 2);
		List<List<NodeContent>> contents = new ArrayList<>();
		// Constructors are package-private, for testing purpose can temporary
		// set them to public
		// Patient p1 = new Patient(2, 1, 3);
		// Patient p2 = new Patient(1, 2, 2);
		// Patient p3 = new Patient(5, 3, 2);
		// Patient p4 = new Patient(4, 4, 1);
		// p3.load();
		// p3.unload(); // pretend p3 is already in hospital
		// /*1*/contents.add(Arrays.asList(new Ambulance(0, 1, null, true), new
		// Ambulance(0, 3, null, true), new Ambulance(0, 5, null, true), new
		// Ambulance(0, 4, null, true)));
		// /*2*/contents.add(Arrays.asList(p2));
		// /*3*/contents.add(Arrays.asList(p1));
		// /*4*/contents.add(Arrays.asList(new Hospital(3, 1, 3)));
		// /*5*/contents.add(Arrays.asList(new Ambulance(4, 2, null, true),
		// p4));
		// /*6*/contents.add(Arrays.asList(p3));
		// /*7*/contents.add(Arrays.asList(new Hospital(6, 2, 3)));

		CityMap map = new CityMap(adjMatrix, null, contents, demands);

		PSOPlanner planner = new PSOPlanner();
		planner.solve(map);
	}

	private PSO pso;
	private PSO.PSOEvaluator evaluator;
	private CityMap map;
	private List<Ambulance> ambulances;
	private List<Patient> patients;
	private List<Hospital> hospitals;

	private int ambCnt;
	private int patCnt;

	private int hosCnt;
	/*
	 * An ambulance is empty - location is real location of the ambulance. An
	 * ambulance has a patient - location is the location of the closest
	 * hospital.
	 */
	private List<Integer> ambLocations;

	private Set<Integer> patInAmb;
	private int particleDims;
	private double[][] particleBounds;
	private int[][] optHospitals;

	private double[][] optHospitalsDist;

	private int[] singleOptHospitals;

	private double[] singleOptHospitalsDist;

	/**
	 * Apply additional heuristics to enhance the existing routes.
	 *
	 * @param plan
	 *            current plan
	 * @return improved plan
	 */
	private Plan applyOptimizations(Plan plan) {
		for (int i = 0; i < plan.routes.length; i++) {
			while (true) {
				Pair<List<Integer>, Double> update = twoOpt(ambulances.get(i), plan.routes[i]);
				if (update == null) {
					break;
				}
				plan.routes[i] = update.x;
				plan.planCost += update.y;
			}
		}
		return plan;
	}

	/**
	 * Required for PSO to initialize first particles correctly.
	 * <p>
	 * Updates <code>particleBounds</code> array.
	 */
	private void buildBounds() {
		particleBounds = new double[particleDims][2];
		for (int i = 0; i < patCnt; i++) {
			particleBounds[i][0] = 0;
			particleBounds[i][1] = 1;
		}
	}

	private Hospital closestHospital(int node) {
		Hospital hos = null;
		double dist = Double.POSITIVE_INFINITY;
		for (Hospital h : hospitals) {
			double curDist = map.shortestDistance(node, h.getNode());
			if (hos == null || dist > curDist) {
				hos = h;
				dist = curDist;
			}
		}
		return hos;
	}

	/**
	 * Transform PSO particle into a valid routes.
	 * <p>
	 * It's the heart of algorithm. For example, the method can be used to
	 * generate a good valid routes from a random particle, without actual use
	 * of PSO.
	 */
	private Plan decodePlan(double[] particle) {
		/*
		 * Sort patients according to their priorities (here priorities are
		 * numbers from patDims, they are not the same as real patient
		 * priorities).
		 */
		int[] patientsSorted = Utils.getSortedIndices(particle);

		/*
		 * Add patients to the routes.
		 */
		Plan plan = new Plan(ambCnt);
		for (int patient : patientsSorted) {
			// Insert the patient into the routes.
			// Test only first few ambulances (currently: at least 2, but not
			// more than 33% or 10)
			double bestInsertionCost = Double.POSITIVE_INFINITY;
			int insertionAmbulance = -1;
			int insertionIndex = -1;
			for (int ambIdx = 0; ambIdx < ambCnt; ambIdx++) {
				Pair<Integer, Double> curInsertion = tryInsert(plan.routes[ambIdx], ambIdx, patient);
				if (curInsertion.y < bestInsertionCost) {
					bestInsertionCost = curInsertion.y;
					insertionAmbulance = ambIdx;
					insertionIndex = curInsertion.x;
				}
			}

			/*
			 * Insert patient into chosen position in the routes.
			 */
			plan.planCost += bestInsertionCost;
			plan.routes[insertionAmbulance].add(insertionIndex, patient);

		}

		/*
		 * Apply optimizations.
		 */
		plan = applyOptimizations(plan);

		return plan;
	}

	/**
	 * Evaluate the routes.
	 *
	 * @param plan
	 *            list of actions
	 * @return value to minimize
	 */
	private double evaluatePlan(Plan plan) {
		return plan.planCost;
	}

	/**
	 * Fill the <code>ambLocations</code> list.
	 */
	private void initAmbLocations() {
		ambLocations = new ArrayList<>();
		patInAmb = new HashSet<>();
		for (int ambIdx = 0; ambIdx < ambulances.size(); ambIdx++) {
			Ambulance amb = ambulances.get(ambIdx);
			if (amb.isFree()) {
				ambLocations.add(amb.getNode());
			} else {
				ambLocations.add(closestHospital(amb.getNode()).getNode());
				patInAmb.add(amb.getPatient().getId());
			}
		}
	}

	/**
	 * Add full path to actions list
	 * 
	 * @param actions
	 *            list where to append actions
	 * @param amb
	 *            current ambulance
	 * @param from
	 *            source node
	 * @param to
	 *            destination node
	 */
	private void insertMoveActions(List<Action> actions, Ambulance amb, int from, int to) {
		List<Integer> path = map.shortestPath(from, to);

		for (int i = 0; i < path.size() - 1; i++) {
			int s = path.get(i);
			int f = path.get(i + 1);
			actions.add(new ActionMove(amb, s, f));
		}
	}

	/**
	 * Check if the given hospital can accept the given patient.
	 */
	private boolean isValidHospital(Patient patient, Hospital hospital) {
		/*
		 * Currently every hospital can take care of any patient.
		 */
		return true;
	}

	/**
	 * Builds <code>optimalHospitals</code> array. Asymptotic: O(patCnt^2 *
	 * hosCnt).
	 * <p>
	 * <code>optHospitals[i][j] - the best hospital to go to with ith patient before going to jth</code>
	 * <code>singleOptHospitals[i] - the best hospital to go to with ith patient</code>
	 */
	private void precalcOptimalHospitals() {
		optHospitals = new int[patCnt][patCnt];
		optHospitalsDist = new double[patCnt][patCnt];
		singleOptHospitals = new int[patCnt];
		singleOptHospitalsDist = new double[patCnt];

		for (int i = 0; i < patCnt; i++) {
			for (int j = 0; j < patCnt; j++) {
				optHospitals[i][j] = -1;
				for (int k = 0; k < hosCnt; k++) {
					if (isValidHospital(patients.get(i), hospitals.get(k))) {
						double curDist = shortestDistance(patients.get(i), hospitals.get(k))
								+ shortestDistance(hospitals.get(k), patients.get(j));
						if (optHospitals[i][j] == -1 || optHospitalsDist[i][j] > curDist) {
							optHospitals[i][j] = k;
							optHospitalsDist[i][j] = curDist;
						}
					}
				}
			}
			singleOptHospitals[i] = -1;
			for (int k = 0; k < hosCnt; k++) {
				if (isValidHospital(patients.get(i), hospitals.get(k))) {
					double curDist = shortestDistance(patients.get(i), hospitals.get(k));
					if (singleOptHospitals[i] == -1 || singleOptHospitalsDist[i] > curDist) {
						singleOptHospitals[i] = k;
						singleOptHospitalsDist[i] = curDist;
					}
				}
			}
		}
	}

	@Override
	public boolean replanAfterDropAction() {
		return false;
	}

	/**
	 *
	 * Update given plan with actions for free ambulances.
	 *
	 * @param plan
	 *            plan to update
	 */
	private void sendFreeAmbsToCentroids(Map<Ambulance, List<Action>> plan) {
		List<Ambulance> freeAmbs = ambulances.stream().filter(a -> !plan.containsKey(a) || plan.get(a).size() == 0)
				.collect(Collectors.toList());
		if (!freeAmbs.isEmpty()) {
			int[] centroids = MaxCoverage.findMaxCoverageLocations(freeAmbs.size(), map);

			int[][] shortestDistances = new int[freeAmbs.size()][centroids.length];
			for (int ambIdx = 0; ambIdx < freeAmbs.size(); ambIdx++) {
				Ambulance amb = freeAmbs.get(ambIdx);
				int ambNode = amb.getNode();
				for (int cen = 0; cen < centroids.length; cen++) {
					shortestDistances[ambIdx][cen] = (int) Math.round(map.shortestDistance(ambNode, centroids[cen]));
				}
			}
			int[] destinations = AssignmentProblemSolver.solve(shortestDistances);
			for (int ambIdx = 0; ambIdx < freeAmbs.size(); ambIdx++) {
				Ambulance amb = freeAmbs.get(ambIdx);
				List<Action> actions = new ArrayList<>();
				insertMoveActions(actions, amb, amb.getNode(), centroids[destinations[ambIdx]]);
				plan.put(amb, actions);
			}

		}
	}

	/**
	 * Wrapper for shortestDistance method in CityMap.
	 */
	private double shortestDistance(NodeContent a, NodeContent b) {
		return map.shortestDistance(a.getNode(), b.getNode());
	}

	@Override
	public Map<Ambulance, List<Action>> solve(CityMap map) {
		this.map = map;

		ambulances = map.getAmbulances();
		patients = map.getPatients().stream().filter(Patient::isWaiting).collect(Collectors.toList());
		hospitals = map.getHospitals();
		ambCnt = ambulances.size();
		patCnt = patients.size();
		hosCnt = hospitals.size();

		// Handle bounds for particles
		particleDims = patCnt;
		buildBounds();

		precalcOptimalHospitals();

		initAmbLocations();

		// Initialize PSO
		evaluator = new VRPEvaluator();
		pso = new PSO(evaluator, particleDims, particleBounds);

		// Find solution
		double[] particle = pso.run(Long.MAX_VALUE);
		Plan solution = decodePlan(particle);
		Map<Ambulance, List<Action>> plan = solution.toMainRepresentation();

		// Send free ambulances to centroids
		sendFreeAmbsToCentroids(plan);

		return plan;
	}

	/**
	 * Find the best spot in the route to insert new patient.
	 * <p>
	 * Method does not change the given list.
	 *
	 * @param ambPlan
	 *            list of patients to visit
	 * @param ambIdx
	 *            index of an ambulance
	 * @param patIdx
	 *            new patient
	 * @return pair of insertion index and planCost delta
	 */
	private Pair<Integer, Double> tryInsert(List<Integer> ambPlan, int ambIdx, int patIdx) {
		Ambulance amb = ambulances.get(ambIdx);
		Patient pat = patients.get(patIdx);
		/*
		 * Special case of empty routes.
		 */
		if (ambPlan.size() == 0)
			return new Pair<>(0, shortestDistance(amb, pat) + singleOptHospitalsDist[patIdx]);
		/*
		 * Find bounds where patient can be placed considering his priority. Can
		 * use two binary searches, but that is unnecessary.
		 */
		int patSeverity = patients.get(patIdx).getSeverity();
		int l = 0;
		int r = 0;
		for (; r < ambPlan.size(); r++) {
			int p = patients.get(ambPlan.get(r)).getSeverity();
			if (patSeverity > p) {
				break;
			} else if (patSeverity < p) {
				l = r + 1;
			}
		}

		/*
		 * Find best spot to insert patient.
		 */
		Pair<Integer, Double> pair = null;
		for (int i = l; i <= r; i++) {
			double diff;
			if (i == 0) {
				int next = ambPlan.get(i);
				Patient nextNode = patients.get(next);
				diff = -shortestDistance(amb, nextNode) + shortestDistance(amb, pat) + optHospitalsDist[patIdx][next];
			} else if (i == ambPlan.size()) {
				int prev = ambPlan.get(i - 1);
				diff = -singleOptHospitalsDist[prev] + optHospitalsDist[prev][patIdx] + singleOptHospitalsDist[patIdx];
			} else {
				int prev = ambPlan.get(i - 1);
				int next = ambPlan.get(i);
				diff = -optHospitalsDist[prev][next] + optHospitalsDist[prev][patIdx] + optHospitalsDist[patIdx][next];
			}
			if (pair == null || pair.y > diff) {
				pair = new Pair<>(i, diff);
			}
		}

		assert pair != null;

		return pair;
	}

	/**
	 * Apply 2-opt optimization to a given plan.
	 * <p>
	 * Tries to reverse some parts of paths.
	 *
	 * @param route
	 *            current route
	 * @return pair of improved route and how much the score is improved, or
	 *         null if no improvement found
	 */
	private Pair<List<Integer>, Double> twoOpt(Ambulance amb, List<Integer> route) {
		if (route.size() < 2)
			return null;
		/*
		 * Precalculate some costs.
		 *
		 * l < r Route from l to r costs prefixSumCost[r] - prefixSumCost[l].
		 * Route from r to l (reversed order) costs revSumCost[l] -
		 * revSumCost[r].
		 */
		double[] prefixSumCost = new double[route.size()];
		for (int i = 1; i < route.size(); i++) {
			prefixSumCost[i] = prefixSumCost[i - 1] + optHospitalsDist[route.get(i - 1)][route.get(i)];
		}
		double[] revSumCost = new double[route.size()];
		for (int i = route.size() - 2; i >= 0; i--) {
			revSumCost[i] = revSumCost[i + 1] + optHospitalsDist[route.get(i + 1)][route.get(i)];
		}

		double bestCostUpd = 0;
		int bestL = -1, bestR = -1;
		for (int l = 0; l < route.size(); l++) {
			int severity = patients.get(route.get(l)).getSeverity();
			for (int r = l + 1; r < route.size() && patients.get(route.get(r)).getSeverity() == severity; r++) {
				// try revert [l; r] patients
				double curCostUpd = 0;
				if (l == 0) {
					curCostUpd -= shortestDistance(amb, patients.get(route.get(l)));
					curCostUpd += shortestDistance(amb, patients.get(route.get(r)));
				} else {
					curCostUpd -= optHospitalsDist[route.get(l - 1)][route.get(l)];
					curCostUpd += optHospitalsDist[route.get(l - 1)][route.get(r)];
				}
				curCostUpd -= prefixSumCost[r] - prefixSumCost[l];
				curCostUpd += revSumCost[l] - revSumCost[r];
				if (r == route.size() - 1) {
					curCostUpd -= singleOptHospitalsDist[route.get(r)];
					curCostUpd += singleOptHospitalsDist[route.get(l)];
				} else {
					curCostUpd -= optHospitalsDist[route.get(r)][route.get(r + 1)];
					curCostUpd += optHospitalsDist[route.get(l)][route.get(r + 1)];
				}
				if (curCostUpd < -1e-7 && curCostUpd < bestCostUpd) {
					bestCostUpd = curCostUpd;
					bestL = l;
					bestR = r;
				}
			}
		}

		if (bestL == -1)
			return null;

		/*
		 * Return best found result.
		 */
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < bestL; i++) {
			result.add(route.get(i));
		}
		for (int i = bestR; i >= bestL; i--) {
			result.add(route.get(i));
		}
		for (int i = bestR + 1; i < route.size(); i++) {
			result.add(route.get(i));
		}
		return new Pair<>(result, bestCostUpd);
	}

}
