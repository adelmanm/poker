import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CsvFileWriter {
	private TreeMap<String,BufferedWriter> nodemap = new TreeMap<String,BufferedWriter>(); //<key, file>
	public static final String COMMA_DELIMITER = ",";
	public static final String NEW_LINE_SEPARATOR = "\n";
	public void write (String filename ,double[][] hist){  	
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
	
	public void write (String filename ,String infoset, double[] strategy){
    	BufferedWriter bw = nodemap.get(filename);
    	try {
	    	if (bw == null) {
				bw = new BufferedWriter(new FileWriter(filename, true));
				nodemap.put(filename, bw);
			}
	    	bw.append(infoset);
	    	bw.append(COMMA_DELIMITER);
            for (int i=0;i<strategy.length;i++){
            	bw.append(String.format("%f",strategy[i]));
            	bw.append(COMMA_DELIMITER);
            }
            bw.append(NEW_LINE_SEPARATOR);         
	        } catch (Exception e) {
	            System.out.println("Error in CsvFileWriter !!!");
	            e.printStackTrace();
	        }  		             	
    }
	
	public void write_game_settings(String filename, int num_players, int total_game_actions, int rounds, int deck_size, int num_player_cards, int num_suits, int hand_size, int flop_size, int ante, int[] bet_sum)
	{
		assert(bet_sum.length == rounds);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
			
			bw.append("num_players"); 						//#0
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(num_players));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("total_game_actions"); 				//#1
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(total_game_actions));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("rounds"); 							//#2
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(rounds));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("deck_size");							//#3
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(deck_size));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("num_player_cards");					//#4
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(num_player_cards));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("num_suits");							//#5
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(num_suits));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("hand_size");							//#6
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(hand_size));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("flop_size");							//#7
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(flop_size));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("ante");								//#8
			bw.append(COMMA_DELIMITER);
			bw.append(String.valueOf(ante));
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.append("bet_sum");							//#9
			for (int i=0; i<rounds; i++) {
				bw.append(COMMA_DELIMITER);
				bw.append(String.valueOf(bet_sum[i]));
			}
			bw.append(NEW_LINE_SEPARATOR);
			
			bw.flush();
			bw.close();	
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
