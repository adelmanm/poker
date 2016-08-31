import java.util.*;
import java.io.File;

public class SimulatorLeduc  
{
	public static final int NUM_PLAYERS = 2;
	public static final int TOTAL_GAME_ACTIONS = 5;
	public static final String log_dir_path = "logs/";
	public static final int ITERAION_GAP = 50;
	public static final boolean UPDATE_STRATEGY_CSV = false;
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		CsvFileWriter CsvWriter = new CsvFileWriter();
		create_logs_dir();
		//TrainCFR_Vanilla trainer= new TrainCFR_Vanilla();
		//TrainCFR_CS trainer= new TrainCFR_CS();
		//TrainCFR_Vanilla_trim_weighted trainer= new TrainCFR_Vanilla_trim_weighted(); //weighted averaging. not working perfectly
		//TrainCFR_Vanilla_trim trainer= new TrainCFR_Vanilla_trim(); //every utility has same weight. currently working
		//TrainCFR_Vanilla_trim_prune trainer= new TrainCFR_Vanilla_trim_prune();
		TrainCFR_Vanilla_prune trainer= new TrainCFR_Vanilla_prune();
		//TrainMCCFR trainer= new TrainMCCFR();
		//TrainMCCFR_trim trainer= new TrainMCCFR_trim();
		double utility[] = new double[NUM_PLAYERS];
		double utility_avg[] = new double[NUM_PLAYERS];
		int num_visited_nodes[] = new int[2];
		int num_iterations = 1000000;
		int max_visited_nodes = 757916; //-1 for no restriction
		int iteration;
		for (iteration = 0; iteration < num_iterations; iteration++)
		{
			for (int player=0;player < NUM_PLAYERS;player++)
			{
				HistoryNodeLeduc h = new HistoryNodeLeduc(NUM_PLAYERS, TOTAL_GAME_ACTIONS);
				utility[player] += trainer.cfr(h,player,iteration,1.0,1.0);
			}
			if (iteration % ITERAION_GAP == 0) {
				System.out.println("iterations passed: " + iteration);
				System.out.println("total decision nodes visited: " + VisitedNodesCounter.to_String());
				if (UPDATE_STRATEGY_CSV == true) {
					trainer.update_strategy_csv(log_dir_path);
					for (int j=0;j<NUM_PLAYERS;j++){
						utility_avg[j] = utility[j] / (iteration+1);
					}
					CsvWriter.write(log_dir_path + "util_hist.csv", utility_avg[0] + "," +utility_avg[1] + "," + String.valueOf(iteration) + ","+   VisitedNodesCounter.to_String());
				}
			}

			//stop if no nodes were visited on this iteration (convergence for prune/trim)
			num_visited_nodes[iteration%2] = VisitedNodesCounter.value();
			if(num_visited_nodes[iteration%2] == num_visited_nodes[(iteration+1)%2]) break;
			if (max_visited_nodes > 0 && max_visited_nodes < num_visited_nodes[iteration%2]) break;
		}
		CsvWriter.flush_close();
		trainer.create_infoset_csv(log_dir_path);
		trainer.print();
		for (int j=0;j<NUM_PLAYERS;j++){
			System.out.print("player " + String.valueOf(j) + " utility:");
			System.out.println(utility[j] / iteration);
		}
		System.out.println("total iterations performed: " + iteration);
		System.out.println("total decision nodes visited: " + VisitedNodesCounter.to_String());
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