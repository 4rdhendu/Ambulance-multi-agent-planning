package model;

public class ActionPick extends Action {

	private Ambulance ambulance;
	private int at;
	private Patient p;

	/**
	 * Builds an {@link ActionPick} object with the specified ambulance, picking
	 * the specified patient at the specified node
	 * 
	 * @param ambulance
	 * @param at
	 * @param patient
	 */
	public ActionPick(Ambulance a, int at, Patient p) {
		ambulance = a;
		this.at = at;
		this.p = p;
	}

	/**
	 * The effects of an {@link ActionPick} are that the ambulance gets loaded
	 * with the patient. For this reason, the ambulance is no more available and
	 * the patient state is not waiting anymore
	 * 
	 * @param cityMap
	 */
	@Override
	protected void applyEffects(CityMap cityMap) {
		ambulance.load(p);
		cityMap.getContentAt(at).remove(p);
		p.load();
	}

	/**
	 * Checks that in the {@link CityMap} passed as parameter the ambulance is
	 * free and is at the location of the patient, whose state must be waiting
	 * 
	 * @param cityMap
	 */
	@Override
	protected void checkPreconditions(CityMap cityMap) {
		if (!(p.isWaiting() && ambulance.isFree() && p.getNode() == at && ambulance.getNode() == at))
			throw new IllegalStateException();
	}

	@Override
	public String toString() {
		return String.format("pick(A%d P%d @ N%d)", ambulance.getId(), p.getId(), at);
	}
}
