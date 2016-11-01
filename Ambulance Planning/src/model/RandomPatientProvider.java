package model;

import java.util.Random;
import java.util.stream.DoubleStream;

public class RandomPatientProvider implements PatientProvider {

	private Random random = new Random(504);
	private double prob;
	private int maxNumberOfPatients;

	private double[] demands;
	private double demandSum;

	public RandomPatientProvider(double prob, int maxNumberOfPatients, CityMap map) {
		this.prob = prob;
		this.maxNumberOfPatients = maxNumberOfPatients;

		demands = map.getDemands().stream().mapToDouble(x -> x).toArray();
		demandSum = DoubleStream.of(demands).sum();
	}

	@Override
	public Patient getNewPatient() {
		maxNumberOfPatients--;
		double demand = random.nextDouble() * demandSum;
		int node = 0;
		while (node < demands.length - 1 && demand > 0) {
			demand -= demands[node];
			node++;
		}
		return new Patient(node, 1 + random.nextInt(3));
	}

	@Override
	public boolean hasNewPatient(boolean planIsEmpty) {
		return maxNumberOfPatients > 0 && (planIsEmpty || random.nextDouble() < prob);
	}
}
