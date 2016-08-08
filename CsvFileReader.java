import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CsvFileReader {
	static final String COMMA_DELIMITER = ",";
	static final String NEW_LINE_SEPARATOR = "\n";
	static private BufferedReader br;
	public void read (String filename ,TreeMap<String,double[]> strategy_profile){
    	try {
    		br = new BufferedReader(new FileReader(filename));
    		String line;
	    	while ((line = br.readLine()) != null) {
				String[] line_split = line.split(COMMA_DELIMITER);
				String infoset = line_split[0];
				double[] strategy = new double[line_split.length-1];
				for (int i=0; i<strategy.length; i++) {
					strategy[i] = Double.parseDouble(line_split[i+1]);
				}
				strategy_profile.put(infoset, strategy);
			}
    	}
    	 catch (IOException  e) {
	        System.out.println("CsvReader: Error in reading " + filename);
	        e.printStackTrace();
	    }     		             	
    }
	
	public void close() {
		try {
			br.close();
        } catch (IOException e) {
            System.out.println("Error while flushing/closing CsvfileWriter !!!");
            e.printStackTrace();
        }
	}
}
