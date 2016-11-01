package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class ManualPatientProvider implements PatientProvider {

	private BufferedReader in;
	private CityMap map;
	private Patient newPatient;
	private Random r;

	public ManualPatientProvider(CityMap map) {
		in = new BufferedReader(new InputStreamReader(System.in));
		this.map = map;
		r = new Random();
	}

	@Override
	public Patient getNewPatient() {
		Patient p = newPatient;
		newPatient = null;
		return p;
	}

	@Override
	public boolean hasNewPatient(boolean planIsEmpty) {
		try {
			System.out.printf(
					"Want to add a patient?\n(enter to continue, r for random, syntax: [node(0-%d) severity(1-3)]* | r, ex: 5 3 7 1)\n > ",
					map.nodesCount());
			String answer = in.readLine();
			if (answer != null && !answer.trim().isEmpty()) {
				// parse user input
				if (answer.trim().equals("r")) {
					newPatient = new Patient(r.nextInt(map.nodesCount()), r.nextInt(3) + 1);
				} else {
					String[] tokens = answer.trim().split(" ");
					for (int i = 0; i < tokens.length; i += 2) {
						int node = Integer.parseInt(tokens[i]);
						int severity = Integer.parseInt(tokens[i + 1]);
						if (node < map.nodesCount() && severity >= 1 && severity <= 3) {
							newPatient = new Patient(node, severity);
						} else {
							System.out.println("Nope");
						}
					}
				}
			}
		} catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
			System.out.println("Input error");
		}
		return newPatient != null;
	}
}
