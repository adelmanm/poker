import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
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
	    	br.close();
    	}
    	 catch (IOException  e) {
	        System.out.println("CsvReader: Error in reading " + filename);
	        e.printStackTrace();
	    }     		             	
    }
	public void read_game_settings(String filename, String[] settings_name, int[] settings_value)
	{
		try {
    		int i=0;
			br = new BufferedReader(new FileReader(filename));
    		String line;
	    	while ((line = br.readLine()) != null) {
	    		String[] line_split = line.split(COMMA_DELIMITER);
				String name = line_split[0];
				settings_name[i] = name;
				int value = Integer.parseInt(line_split[1]);
				settings_value[i] = value;
				i++;
			}
	    	br.close();
    	}
    	 catch (IOException  e) {
	        System.out.println("CsvReader: Error in reading " + filename);
	        e.printStackTrace();
	    }
	}
	
	public void read_bet_sum(String filename, int[] bet_sum, int rounds)
	{
		try {
    		int i=0;
			br = new BufferedReader(new FileReader(filename));
    		String line;
	    	while ((line = br.readLine()) != null) {
	    		String[] line_split = line.split(COMMA_DELIMITER);
				String name = line_split[0];
				if (name.equals("bet_sum")) {
					assert (line_split.length-1 == rounds);
					for (int j=1; j<line_split.length; j++) {
						bet_sum[j-1] = Integer.parseInt(line_split[j]);
					}
					br.close();
					return;
				}
				i++;
			}
	    	br.close();
    	}
    	 catch (IOException  e) {
	        System.out.println("CsvReader: Error in reading " + filename);
	        e.printStackTrace();
	    }
		assert(false); //bet_sum wasn't found
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
