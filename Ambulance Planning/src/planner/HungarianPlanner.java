package planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Action;
import model.ActionDrop;
import model.ActionMove;
import model.ActionPick;
import model.Ambulance;
import model.CityMap;
import model.Patient;

public class HungarianPlanner extends Planner {

	@Override
	public boolean replanAfterDropAction() {
		return true;
	}

	@Override
	public Map<Ambulance, List<Action>> solve(CityMap map) {

		Map<Ambulance, List<Action>> bigplan = new HashMap<Ambulance, List<Action>>();

		// ambs without patient
		int[] availAmbIds = map.getAmbulances().stream().filter(Ambulance::isFree).mapToInt(Ambulance::getId).toArray();
		int availAmb = availAmbIds.length;
		if (availAmb > 0) {
			int[] patIds = map.getPatients().stream().filter(Patient::isWaiting).mapToInt(Patient::getId).toArray();
			int numPat = patIds.length;

			int[] centroids = new int[0];
			if (availAmb > numPat) {
				int numCentroid = availAmb - numPat;
				centroids = MaxCoverage.findMaxCoverageLocations(numCentroid, map);

			}

			// must be a square matrix:
			// - amb x (pat+cen) if pat<=amb
			// - (amb+padding) x pat if pat>amb
			int columnCount = numPat + centroids.length;
			int rowCount = availAmb;
			int[][] shortestDistances = new int[rowCount][columnCount];
			for (int ambIdx = 0; ambIdx < availAmb; ambIdx++) {
				int ambId = availAmbIds[ambIdx];
				Ambulance ambulance = map.getAmbulances().stream().filter(a -> a.getId() == ambId).findAny().get();
				int ambNode = ambulance.getNode();
				for (int pat = 0; pat < numPat; pat++) {
					// total distance amb->pat->hos / severity factor
					int patId = patIds[pat];
					Patient p = map.getPatients().stream().filter(pa -> pa.getId() == patId).findAny().get();
					int patNode = p.getNode();
					int hosNode = map.closestHospital(patNode);

					double dist = map.shortestDistance(ambNode, patNode) + map.shortestDistance(patNode, hosNode);
					shortestDistances[ambIdx][pat] = (int) Math.round(dist * 3 / p.getSeverity());
				}
				for (int cen = 0; cen < centroids.length; cen++) {
					shortestDistances[ambIdx][numPat + cen] = (int) Math
							.round(3 * map.shortestDistance(ambNode, centroids[cen]));
				}
			}

			int[] destinations = AssignmentProblemSolver.solve(shortestDistances);
			for (int ambIdx = 0; ambIdx < availAmb; ambIdx++) {
				int ambId = availAmbIds[ambIdx];
				Ambulance ambulance = map.getAmbulances().stream().filter(a -> a.getId() == ambId).findAny().get();
				List<Action> plan = new ArrayList<>();
				int ambNode = ambulance.getNode();
				int column = destinations[ambIdx];
				if (column < numPat) {
					int patId = patIds[column];
					Patient p = map.getPatients().stream().filter(pa -> pa.getId() == patId).findAny().get();
					int hosNode = map.closestHospital(p.getNode());
					ArrayList<Integer> pathToPat = map.shortestPath(ambNode, p.getNode());
					for (int i = 0; i < pathToPat.size() - 1; i++) {
						plan.add(new ActionMove(ambulance, pathToPat.get(i), pathToPat.get(i + 1)));
					}
					plan.add(new ActionPick(ambulance, p.getNode(), p));

					ArrayList<Integer> pathToHos = map.shortestPath(p.getNode(), hosNode);
					for (int i = 0; i < pathToHos.size() - 1; i++) {
						plan.add(new ActionMove(ambulance, pathToHos.get(i), pathToHos.get(i + 1)));
					}
					plan.add(new ActionDrop(ambulance, hosNode, p));
				} else {
					int cenNode = centroids[column - numPat];
					ArrayList<Integer> pathToCen = map.shortestPath(ambNode, cenNode);
					for (int i = 0; i < pathToCen.size() - 1; i++) {
						plan.add(new ActionMove(ambulance, pathToCen.get(i), pathToCen.get(i + 1)));
					}
				}
				bigplan.put(ambulance, plan);
			}
		}

		// Ambs with a patient
		int[] busyAmbIds = map.getAmbulances().stream().filter(a -> !a.isFree()).mapToInt(Ambulance::getId).toArray();

		for (int ambId : busyAmbIds) {
			Ambulance ambulance = map.getAmbulances().stream().filter(a -> a.getId() == ambId).findAny().get();
			List<Action> plan = new ArrayList<>();
			int ambNode = ambulance.getNode();
			int hosNode = map.closestHospital(ambNode);
			ArrayList<Integer> pathToHos = map.shortestPath(ambNode, hosNode);
			for (int i = 0; i < pathToHos.size() - 1; i++) {
				plan.add(new ActionMove(ambulance, pathToHos.get(i), pathToHos.get(i + 1)));
			}
			plan.add(new ActionDrop(ambulance, hosNode, ambulance.getPatient()));
			bigplan.put(ambulance, plan);
		}

		return bigplan;
	}
}
