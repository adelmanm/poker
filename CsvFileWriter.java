import java.io.FileWriter;
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
	public void write (String filename ,double[] hist){
    	final String COMMA_DELIMITER = ",";
    	final String NEW_LINE_SEPARATOR = "\n";
    	
    	FileWriter fileWriter = null;
    	try {
            fileWriter = new FileWriter(filename);
            for (int i=0;i<hist.length;i++){
            		fileWriter.append(String.format("%.3f",hist[i]));
                	fileWriter.append(COMMA_DELIMITER);
            }
            fileWriter.append(NEW_LINE_SEPARATOR);         
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
}
