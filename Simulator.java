import java.util.*;
import java.io.File;
public class Simulator  
{
	public static final int NUM_PLAYERS = 2;
	public static final int MAX_ACTIONS = 5;
	public static final String log_dir_path = "logs/";
	public static final int ITERAION_GAP = 10;
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		CsvFileWriter CsvWriter = new CsvFileWriter();
		create_logs_dir();
		int num_iterations;
		if (args.length == 0) 
		{
			num_iterations =5000;
		}
		else 
		{
			num_iterations = Integer.valueOf(args[0]);
		}
		System.out.format("num_iterations is %d\n",num_iterations);
		//TrainCFR_Vanilla trainer= new TrainCFR_Vanilla();
		TrainCFR_CS trainer= new TrainCFR_CS();
		//double utilHist[][] = new double[NUM_PLAYERS][num_iterations];
		double utility[] = new double[NUM_PLAYERS];
		double utility_avg[] = new double[NUM_PLAYERS];
		for (int iteration = 0; iteration < num_iterations; iteration++)
		{
			//System.gc();
			for (int player=0;player < NUM_PLAYERS;player++)
			{
				HistoryNode h = new HistoryNode(NUM_PLAYERS, MAX_ACTIONS);
				utility[player] += trainer.cfr(h,player,iteration,1.0,1.0);
				//update average utility history
				/*if (iteration == 0) {
					utilHist[player][iteration] = utility;
				}
				else {
					utilHist[player][iteration] = (utilHist[player][iteration-1] * (iteration - 1) + utility) / iteration;
				}*/
			}
			if (iteration % ITERAION_GAP == 0) {
				System.out.println("iterations passed: " + iteration);
				trainer.update_strategy_csv(log_dir_path);
				for (int j=0;j<NUM_PLAYERS;j++){
					utility_avg[j] = utility[j] / (iteration+1);
				}
				CsvWriter.write(log_dir_path + "util_hist.csv", utility_avg);
			}
			
		}
		trainer.create_infoset_csv(log_dir_path);
		trainer.print();
		System.out.print("player 0 utility:");
		System.out.println(utility[0] / num_iterations);
	    System.out.print("player 1 utility:");
	    System.out.println(utility[1] / num_iterations);
	}
	static void create_logs_dir()
	{
		File log_dir = new File(log_dir_path);
		log_dir.mkdir();
		for (File sub : log_dir.listFiles()) {
            sub.delete();
        }
	}
	
}