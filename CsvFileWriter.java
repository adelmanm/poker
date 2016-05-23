import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class CsvFileWriter {
	public void write (String filename ,double[][] hist){
    	final String COMMA_DELIMITER = ",";
    	final String NEW_LINE_SEPARATOR = "\n";
    	
    	FileWriter fileWriter = null;
    	try {
            fileWriter = new FileWriter(filename);
            for (int i=0;i<hist.length;i++){
            	for (int j=0;j<hist[i].length;j++){
            		fileWriter.append(String.format("%.3f",hist[i][j]));
                	fileWriter.append(COMMA_DELIMITER);
            	}
            	fileWriter.append(NEW_LINE_SEPARATOR);
            }            
	        } catch (Exception e) {
	            System.out.println("Error in CsvFileWriter !!!");
	            e.printStackTrace();
	        } finally {
	            try {
	                fileWriter.flush();
	                fileWriter.close();
	            } catch (IOException e) {
	                System.out.println("Error while flushing/closing fileWriter !!!");
	                e.printStackTrace();
	            }
	        }    		             	
    }
	public void write (String filename ,double[] strategy){
    	final String COMMA_DELIMITER = ",";
    	final String NEW_LINE_SEPARATOR = "\n";
    	
    	BufferedWriter bw = null;
    	try {
            bw = new BufferedWriter(new FileWriter(filename, true));
            for (int i=0;i<strategy.length;i++){
            	bw.append(String.format("%.3f",strategy[i]));
            	bw.append(COMMA_DELIMITER);
            }
            bw.append(NEW_LINE_SEPARATOR);         
	        } catch (Exception e) {
	            System.out.println("Error in CsvFileWriter !!!");
	            e.printStackTrace();
	        } finally {
	            try {
	            	bw.flush();
	            	bw.close();
	            } catch (IOException e) {
	                System.out.println("Error while flushing/closing fileWriter !!!");
	                e.printStackTrace();
	            }
	        }    		             	
    }
	public void write (String filename ,String infoset){
    	final String NEW_LINE_SEPARATOR = "\n";
    	
    	BufferedWriter bw = null;
    	try {
            bw = new BufferedWriter(new FileWriter(filename, true));
            bw.append(infoset);
        	bw.append(NEW_LINE_SEPARATOR);
                    
	        } catch (Exception e) {
	            System.out.println("Error in CsvFileWriter !!!");
	            e.printStackTrace();
	        } finally {
	            try {
	            	bw.flush();
	            	bw.close();
	            } catch (IOException e) {
	                System.out.println("Error while flushing/closing fileWriter !!!");
	                e.printStackTrace();
	            }
	        }    		             	
    }
}
