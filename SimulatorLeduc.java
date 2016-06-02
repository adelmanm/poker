import java.util.*;
import java.io.File;

public class SimulatorLeduc  
{
	public static final int NUM_PLAYERS = 2;
	public static final int MAX_ACTIONS = 5;
	public static final String log_dir_path = "logs/";
	public static final int ITERAION_GAP = 1;
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		CsvFileWriter CsvWriter = new CsvFileWriter();
		create_logs_dir();
		int num_iterations;
		if (args.length == 0) 
		{
			num_iterations =100;
		}
		else 
		{
			num_iterations = Integer.valueOf(args[0]);
		}
		System.out.format("num_iterations is %d\n",num_iterations);
		TrainCFR_Vanilla trainer= new TrainCFR_Vanilla();
		//TrainCFR_CS trainer= new TrainCFR_CS();
		double utility[] = new double[NUM_PLAYERS];
		double utility_avg[] = new double[NUM_PLAYERS];
		for (int iteration = 0; iteration < num_iterations; iteration++)
		{
			for (int player=0;player < NUM_PLAYERS;player++)
			{
				HistoryNodeLeduc h = new HistoryNodeLeduc(NUM_PLAYERS, MAX_ACTIONS);
				utility[player] += trainer.cfr(h,player,iteration,1.0,1.0);
			}
			if (iteration % ITERAION_GAP == 0) {
				//System.out.println("iterations passed: " + iteration);
				trainer.update_strategy_csv(log_dir_path);
				for (int j=0;j<NUM_PLAYERS;j++){
					utility_avg[j] = utility[j] / (iteration+1);
				}
				CsvWriter.write(log_dir_path + "util_hist.csv", utility_avg);
			}
			
		}
		CsvWriter.flush_close();
		trainer.create_infoset_csv(log_dir_path);
		trainer.print();
		for (int j=0;j<NUM_PLAYERS;j++){
			System.out.print("player " + String.valueOf(j) + " utility:");
			System.out.println(utility[j] / num_iterations);
		}
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