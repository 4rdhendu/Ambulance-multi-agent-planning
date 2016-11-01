package planner;

import java.util.List;
import java.util.Map;

import model.Action;
import model.Ambulance;
import model.CityMap;

public abstract class Planner {
	public static List<Action> pickConcurrentActions(List<Action> plan) {
		// TODO find the first action for every ambulance, remove them and
		// return them
		return null;
	}

	/**
	 * Tells if a plan should be evaluated again after a drop action.
	 */
	public abstract boolean replanAfterDropAction();

	public abstract Map<Ambulance, List<Action>> solve(CityMap map);
}
