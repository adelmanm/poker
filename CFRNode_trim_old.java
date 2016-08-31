import java.util.Arrays;

public class CFRNode_trim_old {
	private double[][] regretSum;
	private double[] strategy;
	private double[][] strategySum;
	private boolean[] is_valid;
	private int total_game_actions; //number of actions the information set with most actions. used to set arrays length 
	private int num_valid_actions; //number of actions in this information set.
	private final int iteration_mod = 3; //has to be at least 2 to allow updating only for the next iteration
	private int current_iteration_mod_pointer = 0;
	public static final int UTILITY_HISTORY_LENGTH = 5000;
	public static final double CUTOFF_THRESHOLD = 0.01;
	public static final int NUM_PLAYERS = 2;
	private double total_utility[];
	private int utility_history_counter[];
	private boolean[] trim;
	private double[] mean_square_est; //sums the square of the averege utilities
	private double[] mean_est; //sums the averege utilities
	
	public void Print() {
	    System.out.println(Arrays.toString(getAverageStrategy()));
	}
	
	
	
	CFRNode_trim_old(DecisionNode h){
		total_game_actions = h.total_game_actions();
		num_valid_actions = h.num_valid_actions();
		regretSum = new double[iteration_mod][total_game_actions];
		strategy = new double[total_game_actions];
		strategySum = new double[iteration_mod][total_game_actions];
		is_valid = new boolean[total_game_actions];
		for (int a=0; a < total_game_actions; a++)
		{
			is_valid[a] = h.action_valid(a);
		}
		utility_history_counter = new int[NUM_PLAYERS];
		trim = new boolean[NUM_PLAYERS];
		total_utility = new double[NUM_PLAYERS];
		mean_square_est = new double[NUM_PLAYERS];
		mean_est = new double[NUM_PLAYERS];
		for (int i=0; i< NUM_PLAYERS; i++){
			utility_history_counter[i] = 0;
			total_utility[i] = 0;
			trim[i] = false;
			mean_square_est[i] = 0;
			mean_est[i] = 0;
		}
	}
	public void updateTables(int player, int index, double regret, double pi0, double pi1, int current_iteration) {
		current_iteration_mod_pointer = current_iteration%iteration_mod;
		int next_iteration_mod = (current_iteration+1)%iteration_mod;
		int next_next_iteration_mod = (current_iteration+2)%iteration_mod;
		if (player == 0) {
			regretSum[next_iteration_mod][index] += pi1*regret;
			strategySum[next_iteration_mod][index] += pi0*strategy[index];
		}
		else if (player == 1) {
			regretSum[next_iteration_mod][index] += pi0*regret;
			strategySum[next_iteration_mod][index] += pi1*strategy[index];
		}
		regretSum[next_next_iteration_mod][index] = regretSum[next_iteration_mod][index];
		strategySum[next_next_iteration_mod][index] = strategySum[next_iteration_mod][index];
	}
	public void updateUtility(double utility, int player){
		utility_history_counter[player]++;
		total_utility[player] = total_utility[player] + utility;
		double mean = get_mean(player);
		mean_square_est[player] += mean*mean;
		mean_est[player] += mean;
		if (utility_history_counter[player] % UTILITY_HISTORY_LENGTH == 0) {
			double var = get_var(player);
			if (get_var(player) < CUTOFF_THRESHOLD) {
				trim[player] = true;
			}
			else {
				mean_square_est[player] = 0;
				mean_est[player] = 0;
			}
		}
	}
	
	public boolean can_trim(int player) {
		if (trim[player] == true) return true;
		else return false;
	}
	
	public double get_mean(int player) { //returns avegare utility
		return total_utility[player] / utility_history_counter[player];
	}
	
	public double get_mean_est(int player) { //returns the average of the average utilities
		return mean_est[player] / UTILITY_HISTORY_LENGTH;
	}
	
	private double get_var(int player)  {
		double mean_average = get_mean_est(player);
		double var = mean_square_est[player]/ UTILITY_HISTORY_LENGTH - mean_average*mean_average;
		return var;
	}
	
	public double[] getStrategy(int current_iteration) 
	{
		double normalizingSum = 0.0;
		int current_iteration_mod = current_iteration%iteration_mod; 		for (int a=0; a < total_game_actions; a++)
		{
			if (is_valid[a] == false) continue;
			strategy[a] = regretSum[current_iteration_mod][a] > 0 ? regretSum[current_iteration_mod][a] : 0;
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
		int next_iteration_mod_pointer = (current_iteration_mod_pointer+1)%iteration_mod;		for (int a=0; a < total_game_actions; a++){
			if (is_valid[a] == false) continue;
			normalizingSum += strategySum[next_iteration_mod_pointer][a];
		}
		for (int a=0; a < total_game_actions; a++){
			if (is_valid[a] == false) continue;
			if (normalizingSum > 0) {
				avgStrategy[a] = strategySum[next_iteration_mod_pointer][a] / normalizingSum;
			}
			else {
				avgStrategy[a] = 1.0 /num_valid_actions;
			}
		}
		return avgStrategy;
	}
}
