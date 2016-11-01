import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

class JsonInput {
    /**
     * Methods coded for the problem
     * @author Team 14
     */
    
    /**
     * Parse the input file and store the values. 
     * Catches exceptions if something goes wrong (no input file, wrong arguments...)
     * 
     * @param path Input file
     * @param city Storage array (numb nodes, numb roads, max noise, max demand)
     * @param patient Storage array (numb patients, prob of priorities)
     * @param objs Storage array (numb ambulances, numb hospitals)
     * @return String with output path
     */
    public static ProblemParameters readArguments(String path){
		try {
			ProblemParameters result = new ProblemParameters();
			JSONObject o = new JSONObject(new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8));

			// Random cities
	    	result.nodes = o.getInt("nodes"); // Number of nodes
	    	result.roads = o.getInt("roads"); // Number of roads
	    	result.noise = o.getDouble("noise"); // Max noise
	    	result.demand = o.getInt("demand"); // Max demand
			
			// Patients
	    	result.patients = o.getInt("patients"); // Number of patients
	    	result.severity1prob = o.getDouble("severity1prob"); // Prob for priority 1
	    	result.severity2prob = o.getDouble("severity2prob"); // Prob for priority 2
	    	result.severity3prob = o.getDouble("severity3prob"); // Prob for priority 3
	    	
			// Ambulance and hospitals
	    	result.hospitals = o.getInt("hospitals");;
	    	result.ambulances = o.getInt("ambulances");  
	    	
	    	// Output
	    	result.output = o.getString("output");
	    	
	    	return result;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }
}