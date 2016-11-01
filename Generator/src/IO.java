/** Simple yet moderately fast I/O routines.
 *
 * Example usage:
 *
 * Kattio io = new Kattio(System.in, System.out);
 *
 * while (io.hasMoreTokens()) {
 *    int n = io.getInt();
 *    double d = io.getDouble();
 *    double ans = d*n;
 *
 *    io.println("Answer: " + ans);
 * }
 *
 * io.close();
 *
 *
 * Some notes:
 *
 * - When done, you should always do io.close() or io.flush() on the
 *   Kattio-instance, otherwise, you may lose output.
 *
 * - The getInt(), getDouble(), and getLong() methods will throw an
 *   exception if there is no more data in the input, so it is generally
 *   a good idea to use hasMoreTokens() to check for end-of-file.
 *
 * @author: Kattis
 */


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

class IO extends PrintWriter {
		
    public IO(InputStream i) {
        super(new BufferedOutputStream(System.out));
        r = new BufferedReader(new InputStreamReader(i));
    }
    
    public IO(OutputStream o) {
        super(new BufferedOutputStream(o));
    }

    public boolean hasMoreTokens() {
        return peekToken() != null;
    }

    public int getInt() {
        return Integer.parseInt(nextToken());
    }

    public double getDouble() {
        return Double.parseDouble(nextToken());
    }

    public float getFloat() {
        return Float.parseFloat(nextToken());
    }

    public long getLong() {
        return Long.parseLong(nextToken());
    }

    public String getWord() {
        return nextToken();
    }



    private BufferedReader r;
    private String line;
    private StringTokenizer st;
    private String token;

    private String peekToken() {
        if (token == null)
            try {
                while (st == null || !st.hasMoreTokens()) {
                    line = r.readLine();
                    if (line == null) return null;
                    st = new StringTokenizer(line);
                }
                token = st.nextToken();
            } catch (IOException e) { }
        return token;
    }

    private String nextToken() {
        String ans = peekToken();
        token = null;
        return ans;
    }
    
    
    
    
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
    public static String readArguments(String path, double[] city, double[] patient, int[] objs){
    	
    	IO reader;
    	String out = "";
		try {
			InputStream input = new FileInputStream(path);
			reader = new IO(input);
			
			// Random cities
	    	city[0] = reader.getInt(); // Number of nodes
	    	city[1] = reader.getInt(); // Number of roads
	    	city[2] = reader.getDouble(); // Max noise
	    	city[3] = reader.getInt(); // Max demand
			
			// Patients
	    	patient[0] = reader.getInt(); // Number of patients
	    	patient[1] = reader.getDouble(); // Prob for priority 1
	    	patient[2] = reader.getDouble(); // Prob for priority 2
	    	patient[3] = reader.getDouble(); // Prob for priority 3
	    	
			// Ambulance and hospitals
	    	objs[0] = reader.getInt();
	    	objs[1] = reader.getInt();  
	    	out = reader.getWord();
	    	
	    	reader.close();
	    	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return out;
    }
    
    /**
     * Writes all the information generated into the output file
     * Catches exceptions if something goes wrong
     * 
     * @param outpath Path and name of the output file
     * @param content Generated environment in PDDL
     */
    public static void printPDDL(String outpath, String content) {
    	
    	IO writer;
    	try {
    		OutputStream output = new FileOutputStream(outpath);
    		writer = new IO(output);
    		writer.write(content);
    		writer.close();
    		
		} catch (Exception ex) {
			ex.printStackTrace();
		}

    }
}