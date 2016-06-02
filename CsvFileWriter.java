import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CsvFileWriter {
	private TreeMap<String,BufferedWriter> nodemap = new TreeMap<String,BufferedWriter>(); //<key, file>
	public void write (String filename ,double[][] hist){
    	final String COMMA_DELIMITER = ",";
    	final String NEW_LINE_SEPARATOR = "\n";
    	
    	BufferedWriter bw = nodemap.get(filename);
    	try {
    		if (bw == null) {
				bw = new BufferedWriter(new FileWriter(filename, true));
				nodemap.put(filename, bw);
			}
            for (int i=0;i<hist.length;i++){
            	for (int j=0;j<hist[i].length;j++){
            		bw.append(String.format("%.3f",hist[i][j]));
                	bw.append(COMMA_DELIMITER);
            	}
            	bw.append(NEW_LINE_SEPARATOR);
            }            
	        } catch (Exception e) {
	            System.out.println("Error in CsvFileWriter !!!");
	            e.printStackTrace();
	        }     		             	
    }
	public void write (String filename ,double[] strategy){
    	final String COMMA_DELIMITER = ",";
    	final String NEW_LINE_SEPARATOR = "\n";
    	
    	BufferedWriter bw = nodemap.get(filename);
    	try {
	    	if (bw == null) {
				bw = new BufferedWriter(new FileWriter(filename, true));
				nodemap.put(filename, bw);
			}
            for (int i=0;i<strategy.length;i++){
            	bw.append(String.format("%.3f",strategy[i]));
            	bw.append(COMMA_DELIMITER);
            }
            bw.append(NEW_LINE_SEPARATOR);         
	        } catch (Exception e) {
	            System.out.println("Error in CsvFileWriter !!!");
	            e.printStackTrace();
	        }  		             	
    }
	public void write (String filename ,String infoset){
    	final String NEW_LINE_SEPARATOR = "\n";
    	
    	BufferedWriter bw = nodemap.get(filename);
    	try {
    		if (bw == null) {
				bw = new BufferedWriter(new FileWriter(filename, true));
				nodemap.put(filename, bw);
			}
            bw.append(infoset);
        	bw.append(NEW_LINE_SEPARATOR);
                    
	        } catch (Exception e) {
	            System.out.println("Error in CsvFileWriter !!!");
	            e.printStackTrace();
	        }     		             	
    }
	
	public void flush_close() {
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
		try {
			while(i.hasNext()) {
		         Map.Entry me = (Map.Entry)i.next();
		         BufferedWriter bw = (BufferedWriter)me.getValue();
		         bw.flush();
		         bw.close();
			}
        } catch (IOException e) {
            System.out.println("Error while flushing/closing CsvfileWriter !!!");
            e.printStackTrace();
        }
	}
	
	public void flush() {
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
		try {
			while(i.hasNext()) {
		         Map.Entry me = (Map.Entry)i.next();
		         BufferedWriter bw = (BufferedWriter)me.getValue();
		         bw.flush();
			}
        } catch (IOException e) {
            System.out.println("Error while flushing/closing CsvfileWriter !!!");
            e.printStackTrace();
        }
	}
}
