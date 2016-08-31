import java.util.*;
import java.io.File;

public class SimulatorLeduc_Big  
{
	public static final int NUM_PLAYERS = 2;		//2 in standard leduc. number of players/
	public static final int TOTAL_GAME_ACTIONS = 5; //5 in standard leduc. total number of different actions in the game
	public static final int ROUNDS=3; 				//2 in standard leduc. number of betting rounds, including the first blind one.
	public static final int DECK_SIZE=10;  			//6 in standard leduc. total number of cards
	public static final int NUM_PLAYER_CARDS = 1;	//1 in standard leduc. number of cards per player
	public static final int NUM_SUITS = 2;			//2 in standard leduc. number of different card suits (cards with the same number)
	public static final int HAND_SIZE = 2;			//2 in standard leduc. size of the poker hand (can be composed from player and community cards)
	public static final int FLOP_SIZE = 1;			//1 in standard leduc. number of community cards revealed after the first round (only 1 card in later rounds)
	public static final int ANTE = 1;				//1 in standard leduc. the money each player pays at the beginning of the game
	public static final int[] BET_SUM  = {1,1,1};		//{1,1} in standard leduc. amount of bet per round.
	public static final String log_dir_path = "logs/";
	public static final String game_settings_fliename = "game_settings.csv";
	public static final int ITERAION_GAP = 1;
	public static final boolean UPDATE_STRATEGY_CSV = false;
	public static final boolean UPDATE_UTILITY_CSV = true;
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		CsvFileWriter CsvWriter = new CsvFileWriter();
		create_logs_dir();
		write_game_settings(CsvWriter);
		TrainCFR_Vanilla trainer= new TrainCFR_Vanilla();
		//TrainCFR_CS trainer= new TrainCFR_CS();
		//TrainCFR_Vanilla_trim_weighted trainer= new TrainCFR_Vanilla_trim_weighted(); //weighted averaging. not working perfectly
		//TrainCFR_Vanilla_trim trainer= new TrainCFR_Vanilla_trim(); //every utility has same weight. currently working
		//TrainCFR_Vanilla_trim_prune trainer= new TrainCFR_Vanilla_trim_prune();
		//TrainCFR_Vanilla_prune trainer= new TrainCFR_Vanilla_prune();
		//TrainMCCFR trainer= new TrainMCCFR();
		//TrainMCCFR_trim trainer= new TrainMCCFR_trim();
		double utility[] = new double[NUM_PLAYERS];
		double utility_avg[] = new double[NUM_PLAYERS];
		int num_visited_nodes[] = new int[2];
		int num_iterations = 100000000;
		int max_visited_nodes = 50000000; //-1 for no restriction
		int iteration;
		for (iteration = 0; iteration < num_iterations; iteration++)
		{
			for (int player=0;player < NUM_PLAYERS;player++)
			{
				HistoryNodeLeduc_Big h = new HistoryNodeLeduc_Big();
				utility[player] += trainer.cfr(h,player,iteration,1.0,1.0);
			}
			if (iteration % ITERAION_GAP == 0) {
				System.out.println("iterations passed: " + (iteration+1));
				System.out.println("total decision nodes visited: " + VisitedNodesCounter.to_String());
				if (UPDATE_STRATEGY_CSV == true) {
					trainer.update_strategy_csv(log_dir_path);
				}
				if (UPDATE_UTILITY_CSV == true) {
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
	static void write_game_settings(CsvFileWriter CsvWriter)
	{
		CsvWriter.write_game_settings(log_dir_path + game_settings_fliename, NUM_PLAYERS, TOTAL_GAME_ACTIONS, ROUNDS, DECK_SIZE, NUM_PLAYER_CARDS, NUM_SUITS, HAND_SIZE, FLOP_SIZE,ANTE,BET_SUM);
	}
}