package model;

public interface PatientProvider {

	/**A new patient if available
	 * @return
	 */
	public abstract Patient getNewPatient();

	
	/**Checks if this provider has a new patient ready to be spawn
	 * @return
	 */
	public abstract boolean hasNewPatient(boolean planIsEmpty);

}
