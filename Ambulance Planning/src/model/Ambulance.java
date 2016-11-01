/**
 *
 */
package model;

/**
 * @author federico
 *
 */
public class Ambulance extends NodeContent {
	private static int ID = 0;

	private final int id;
	private Patient patient;
	private boolean clean;

	/**
	 * Builds an {@link Ambulance} at the node passed as parameter. The id is
	 * autoincremented and the ambulance is initially free
	 * 
	 * @param node
	 */
	Ambulance(int node) {
		this(node, ID++, null, true);
	}

	/**
	 * Builds an {@link Ambulance} at the node passed as parameter. The provided
	 * id is used and the ambulance is set to be carrying the patient passed as
	 * parameter.
	 * 
	 * @param node
	 */
	Ambulance(int node, int id, Patient patient, boolean clean) {
		super(node);
		this.id = id;
		this.patient = patient;
		this.clean = clean;

		ID = Math.max(ID, id + 1); // avoid id collisions, ID is always greater
									// than max id
	}

	/**
	 * Cleans the ambulance
	 */
	void clean() {
		if (!isFree())
			throw new IllegalStateException("Can not clean with patient onboard");
		clean = true;
	}

	@Override
	public int getId() {
		return id;
	}

	public Patient getPatient() {
		return patient;
	}

	public boolean isClean() {
		return clean;
	}

	public boolean isFree() {
		return patient == null;
	}

	void load(Patient patient) {
		if (!isFree())
			throw new IllegalStateException("Patient already on board");
		// if (!isClean()) {
		// throw new IllegalStateException("Ambulance not clean");
		// }
		this.patient = patient;
		clean = false;
	}

	@Override
	public String toString() {
		return String.format("A%d @ N%d [%s] %s", id, getNode(), isFree() ? "" : patient.toString(),
				isClean() ? "" : "*");
	}

	/**
	 * Unloads the patients, resetting the state of the ambulance to free
	 * 
	 * @return
	 */
	Patient unload() {
		if (isFree())
			throw new IllegalStateException("No patient onboard");
		Patient p = patient;
		patient = null;
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ambulance other = (Ambulance) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
