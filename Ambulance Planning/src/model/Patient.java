package model;

public class Patient extends NodeContent {
	private static int COUNT = 0;

	private final int id;
	private final int severity;

	private boolean waiting = true;
	private boolean inHospital = false;

	/**Builds a new {@link Patient} at the location provided and with the severity passed as parameter.
	 * The id is autoincremented.
	 * @param node
	 * @param severity
	 */
	public Patient(int node, int severity) {
		this(node, -1, severity);
	}

	/**Builds a new {@link Patient} at the location provided and with the severity passed as parameter.
	 * The provided id is used.
	 * @param node
	 * @param id
	 * @param severity
	 */
	Patient(int node, int id, int severity) {
		super(node);
		this.id = id == -1 ? COUNT : id;
		COUNT++;
		this.severity = severity;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getSeverity() {
		return severity;
	}

	public boolean isInHospital() {
		return inHospital;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void load() {
		waiting = false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("P%d (%d) ", id, severity));
		if (inHospital) {
			sb.append("SAVED");
		} else if (waiting) {
			sb.append("@ N" + getNode());
		} else {
			sb.append("TRAVELLING");
		}
		return sb.toString();
	}

	public void unload() {
		inHospital = true;
	}
}
