package model;

public class ActionDrop extends Action {

	private Ambulance ambulance;
	private int at;
	private Patient p;

	/**
	 * Builds an {@link ActionDrop} object with the specified ambulance,
	 * dropping the specified patient at the specified node
	 * 
	 * @param ambulance
	 * @param at
	 * @param patient
	 */
	public ActionDrop(Ambulance ambulance, int at, Patient patient) {
		this.ambulance = ambulance;
		this.at = at;
		this.p = patient;
	}

	/**
	 * The effects of an {@link ActionDrop} are that the ambulance gets unloaded
	 * from its patient. For this reason, the ambulance is now available and the
	 * patient is saved
	 * 
	 * @param cityMap
	 */
	@Override
	protected void applyEffects(CityMap cityMap) {
		ambulance.unload();
		p.unload();
	}

	/**
	 * Checks that in the {@link CityMap} passed as parameter the ambulance
	 * contains a patient and is at the location of an hospital
	 * 
	 * @param cityMap
	 */
	@Override
	protected void checkPreconditions(CityMap cityMap) {
		if (!(ambulance.getNode() == at && cityMap.getContentAt(at).stream().anyMatch(c -> c instanceof Hospital)
				&& ambulance.getPatient() == p))
			throw new IllegalStateException();
	}

	@Override
	public String toString() {
		return String.format("drop(A%d P%d @ N%d)", ambulance.getId(), p.getId(), at);
	}
}
