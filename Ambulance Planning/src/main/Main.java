package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import model.*;
import model.CityMap.Print;
import planner.HungarianPlanner;
import planner.Planner;

public class Main {
	
	private static double totalDistance = 0.0;
	private static long totalWaitingTime = 0;
	
	public static void main(String[] args) throws Exception {
		// parsing the problem description from the command line
		String cityFileName = args[0];

		// debugging utilities
		new File("logs").mkdir();
		String now = new SimpleDateFormat(".yyyy-MM-dd_HH.mm.ss").format(new Date());
		String easyToReadDescription = "logs/" + cityFileName.split(".pddl")[0] + now + ".descr";
		String solution = "logs/" + cityFileName.split(".pddl")[0] + now + ".plan";

		// initial set up
		Planner planner = (Planner) Class.forName(args[1]).getConstructor().newInstance();
		CityMap map = CityParser.parse(cityFileName);
		Files.copy(new File(cityFileName).toPath(), new File(easyToReadDescription).toPath());

		PrintWriter solutionWriter = new PrintWriter(solution);
		PrintWriter eventsWriter = new PrintWriter(easyToReadDescription);

		PatientProvider pProvider;
		if (args.length == 2) {
			pProvider = new ManualPatientProvider(map);
		} else {
			pProvider = new RandomPatientProvider(Double.parseDouble(args[2]), Integer.parseInt(args[3]), map);
		}

		System.out.println(map.represent(Print.ADJ_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_DISTANCES_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_PATHS));
		System.out.println(map.represent(Print.AMBULANCES_LOCATIONS));
		System.out.println(map.represent(Print.HOSPITAL_LOCATIONS));
		System.out.println(map.represent(Print.PATIENT_LOCATIONS));
		System.out.println(map.represent(Print.DEMANDS));

		eventsWriter.println(map.represent(Print.ADJ_MATRIX));
		eventsWriter.println(map.represent(Print.SHORTEST_DISTANCES_MATRIX));
		eventsWriter.println(map.represent(Print.SHORTEST_PATHS));
		eventsWriter.println(map.represent(Print.HOSPITAL_LOCATIONS));
		eventsWriter.println(map.represent(Print.DEMANDS));
		eventsWriter.println("\n--------------------------------------------------\n");

		Map<Ambulance, List<Action>> plan = null;
		int step = 0;
		boolean replanningNeeded = true;
		do {
			System.out.println("\n--------------------------------------------------");
			System.out.println("Step: " + step);
			solutionWriter.println("Step:" + step);
			eventsWriter.println("Step:" + step);
			step++;

			// if we don't have a plan make one
			if (replanningNeeded) {
				System.out.println("Replanning...");
				eventsWriter.println("  Replanning...");
				plan = planner.solve(map);
				replanningNeeded = false;
			}

			System.out.print(map.represent(Print.AMBULANCES_LOCATIONS));
			System.out.print(map.represent(Print.PATIENT_LOCATIONS));

			// print full plan
			for (Ambulance amb : plan.keySet()) {
				System.out.println("Actions for " + amb);
				if (!plan.get(amb).isEmpty()) {
					for (Action a : plan.get(amb)) {
						System.out.println("   " + a);
					}
				} else {
					System.out.println("   nop");
				}
			}

			for (Ambulance amb : plan.keySet()) {
				if (!plan.get(amb).isEmpty()) {
					Action a = plan.get(amb).remove(0);
					System.out.println("Executing: " + a);
					solutionWriter.println("  " + a);
					eventsWriter.println("  " + a);
					map.performAction(a);
					if (a instanceof ActionMove) {
						int from = ((ActionMove) a).getFrom();
						int to= ((ActionMove) a).getTo();
						totalDistance += map.shortestDistance(from, to);
					}
					if (a instanceof ActionDrop && planner.replanAfterDropAction()) {
						replanningNeeded = true;
					}
				}
			}
			
			totalWaitingTime += map.getPatients().stream().filter(Patient::isWaiting).count();

			boolean planIsEmpty = plan.values().stream().allMatch(List::isEmpty);

			if (pProvider.hasNewPatient(planIsEmpty)) {
				Patient patient = pProvider.getNewPatient();
				System.out.println("Added " + patient);
				eventsWriter.println("  Added " + patient);
				map.spawn(patient);
				replanningNeeded = true;
			}
		} while (replanningNeeded || !plan.values().stream().allMatch(List::isEmpty));

		System.out.println("\nDone!");
		System.out.println(map.represent(Print.AMBULANCES_LOCATIONS));
		System.out.println("\nMetrics:");
		System.out.println("  Total distance travelled: " + totalDistance);
		System.out.println("  Total time patients waited: " + totalWaitingTime);
		

		eventsWriter.println("\nDone!");
		eventsWriter.println(map.represent(Print.AMBULANCES_LOCATIONS));
		eventsWriter.println("\nMetrics:");
		eventsWriter.println("  Total distance travelled: " + totalDistance);
		eventsWriter.println("  Total time patients waited: " + totalWaitingTime);

		solutionWriter.close();
		eventsWriter.close();
	}
}
