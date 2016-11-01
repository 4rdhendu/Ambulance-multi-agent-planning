/**
 * Generator of PDDL files fitting our domain
 * @author Team 14
 *
 */
public class Main {

	/**
	 * Main that reads file, generates problem and prints PDDL files
	 * @param args The input file must follow this structure:
	 * - number of nodes
	 * - number of roads
	 * - max noise
	 * - max demand
	 * 
	 * - number of patients
	 * - prob priority 1
	 * - prob priority 2
	 * - prob priority 3
	 * 
	 * - number of ambulances
	 * - number of hospitals
	 * 
	 * - output path
	 */
	public static void main(String[] args) {
		
		/* Arguments control error */
		if(args.length != 1)
			throw new IllegalArgumentException();
		
		/* Read file and check arguments */
		ProblemParameters params = JsonInput.readArguments(args[0]);
		
		/* Generate problem */
		String s = Generator.generateProblem(params);
		
		/* Write output PDDL */
		IO.printPDDL(params.output, s);
	}
}
