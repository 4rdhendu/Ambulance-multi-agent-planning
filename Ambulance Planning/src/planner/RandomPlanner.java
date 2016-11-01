package planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Action;
import model.ActionMove;
import model.Ambulance;
import model.CityMap;

public class RandomPlanner extends Planner {

	@Override
	public boolean replanAfterDropAction() {
		return true;
	}

	@Override
	public Map<Ambulance, List<Action>> solve(CityMap map) {
		int from = map.getAmbulances().get(0).getNode();
		int to = (int) map.adjacentNodes(from).toArray()[0];
		Ambulance amb = map.getAmbulances().get(0);

		List<Action> list = new ArrayList<>();
		list.add(new ActionMove(amb, from, to));

		Map<Ambulance, List<Action>> result = new HashMap<>();
		result.put(amb, list);
		return result;
	}
}
