package model;

public class ActionMove extends Action {

	private Ambulance ambulance;
	private int from;
	private int to;
	
	public Ambulance getAmbulance() {
		return ambulance;
	}
	
	public int getFrom() {
		return from;
	}
	
	public int getTo() {
		return to;
	}

	/**
	 * Builds an {@link ActionMove} for the specified ambulance from an initial
	 * node and an adjacent destination node
	 * 
	 * @param ambulance
	 * @param from
	 * @param to
	 */
	public ActionMove(Ambulance ambulance, int from, int to) {
		this.ambulance = ambulance;
		this.from = from;
		this.to = to;
	}

	/**
	 * The effect of an {@link ActionMove} are that the ambulance is no more at
	 * the initial node and is at the destination node
	 * 
	 * @param cityMap
	 */
	@Override
	protected void applyEffects(CityMap cityMap) {
		cityMap.getContentAt(from).remove(ambulance);
		ambulance.setNode(to);
		cityMap.getContentAt(to).add(ambulance);
	}

	/**
	 * The preconditions od an {@link ActionMove} are that the ambulance is at
	 * the initial node and the destination node is adjaccent to the initial
	 * node
	 * 
	 * @param cityMap
	 */
	@Override
	protected void checkPreconditions(CityMap cityMap) {
		if (!(ambulance.getNode() == from && cityMap.areAdjacent(from, to)))
			throw new IllegalStateException();
	}

	@Override
	public String toString() {
		return String.format("move(A%d %d -> %d)", ambulance.getId(), from, to);
	}
}
