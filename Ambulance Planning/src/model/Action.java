package model;

public abstract class Action {
	/**
	 * Applies the effect of this action object on the {@link CityMap} object
	 * 
	 * @param cityMap
	 */
	abstract protected void applyEffects(CityMap cityMap);

	/**
	 * Checks the preconditions of this action object on the {@link CityMap}
	 * object
	 * 
	 * @param cityMap
	 */
	abstract protected void checkPreconditions(CityMap cityMap);

	/**
	 * Performs the action described by this object on the {@link CityMap}
	 * object, checking the preconditions first and applying the effecets later
	 * 
	 * @param cityMap
	 */
	void performAction(CityMap cityMap) {
		checkPreconditions(cityMap);
		applyEffects(cityMap);
	}
}
