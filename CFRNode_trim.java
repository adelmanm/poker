import java.util.Arrays;

public class CFRNode_trim {
	private double[] regretSum;
	private double[] strategy;
	private double[] strategySum;
	private boolean[] is_valid;
	private double[][] utility_history;
	private int total_game_actions; //number of actions the information set with most actions. used to set arrays length 
	private int num_valid_actions; //number of actions in this information set.

	
	public static final int UTILITY_HISTORY_LENGTH = 100;
	public static final double CUTOFF_THRESHOLD = 0.0001;
	public static final int NUM_PLAYERS = 2;
	private double total_utility[];
	private int update_iterations[];
	private int utility_history_counter[];
	private boolean utility_var_valid[];

	
	public void Print() {
	    System.out.println(Arrays.toString(getAverageStrategy()));
	}
	
	
	
	CFRNode_trim(DecisionNode h){
		total_game_actions = h.total_game_actions();
		num_valid_actions = h.num_valid_actions();
		regretSum = new double[total_game_actions];
		strategy = new double[total_game_actions];
		strategySum = new double[total_game_actions];
		is_valid = new boolean[total_game_actions];
		for (int a=0; a < total_game_actions; a++)
		{
			is_valid[a] = h.action_valid(a);
		}
		utility_history = new double[NUM_PLAYERS][UTILITY_HISTORY_LENGTH];
		utility_history_counter = new int[NUM_PLAYERS];
		utility_var_valid = new boolean[NUM_PLAYERS];
		total_utility = new double[NUM_PLAYERS];
		update_iterations = new int[NUM_PLAYERS];
		for (int i=0; i< NUM_PLAYERS; i++){
			utility_history_counter[i] = 0;
			utility_var_valid[i] = false;
			total_utility[i] = 0;
		}
	}
	public void updateTables(int player, int index, double regret, double pi0, double pi1) {
		if (player == 0) {
			regretSum[index] += pi1*regret;
			strategySum[index] += pi0*strategy[index];
		}
		else if (player == 1) {
			regretSum[index] += pi0*regret;
			strategySum[index] += pi1*strategy[index];
		}	
	}
	public void updateUtility(double utility, int player){
		total_utility[player] = total_utility[player] - utility_history[player][utility_history_counter[player]] + utility;
		update_iterations[player]++;
		//utility_history[utility_history_counter] = total_utility[player] / update_iterations[player];
		utility_history[player][utility_history_counter[player]] = utility;
		utility_history_counter[player]++;
		if (utility_history_counter[player] == UTILITY_HISTORY_LENGTH) {
			utility_var_valid[player] = true;
			utility_history_counter[player] = 0;	
		}
	}
	
	public boolean can_trim(int player) {
		if (utility_var_valid[player] == true && get_var(player) < CUTOFF_THRESHOLD) return true;
		else return false;
	}
	
	public double get_mean(int player) {
		return total_utility[player] / UTILITY_HISTORY_LENGTH;
	}
	
	private double get_var(int player)  {
		double mean = get_mean(player);
		double var = 0.0;
		for (int i=0; i<UTILITY_HISTORY_LENGTH; i++){
			var += (utility_history[player][i]-mean)*(utility_history[player][i]-mean);
		}
		return var/UTILITY_HISTORY_LENGTH;
	}
	
	public double[] getStrategy() 
	{
		double normalizingSum = 0.0;
		for (int a=0; a < total_game_actions; a++)
		{
			if (is_valid[a] == false) continue;
			strategy[a] = regretSum[a] > 0 ? regretSum[a] : 0;
			normalizingSum += strategy[a];
		}
		for (int a=0; a < total_game_actions; a++)
		{
			if (is_valid[a] == false) continue;
			if (normalizingSum > 0)
			{
				strategy[a] /= normalizingSum;
			}
			else 
			{
				strategy[a] = 1.0 /num_valid_actions;
			}
		}
		return strategy;
	}
	public double[] getAverageStrategy () {
		double[] avgStrategy = new double[total_game_actions];
		double normalizingSum = 0.0;
		for (int a=0; a < total_game_actions; a++){
			if (is_valid[a] == false) continue;
			normalizingSum += strategySum[a];
		}
		for (int a=0; a < total_game_actions; a++){
			if (is_valid[a] == false) continue;
			if (normalizingSum > 0) {
				avgStrategy[a] = strategySum[a] / normalizingSum;
			}
			else {
				avgStrategy[a] = 1.0 /num_valid_actions;
			}
		}
		return avgStrategy;
	}
}
