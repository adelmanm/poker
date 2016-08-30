import java.util.Arrays;

public class MCCFRNode {
	private double[][] regretSum;
	private double[] strategy;
	private double[][] strategySum;
	private boolean[] is_valid;
	private int total_game_actions; //number of actions the information set with most actions. used to set arrays length 
	private int num_valid_actions; //number of actions in this information set.
	private final int iteration_mod = 3; //has to be at least 2 to allow updating only for the next iteration
	private int current_iteration_mod_pointer = 0;
	
	public void Print() {
	    System.out.println(Arrays.toString(getAverageStrategy()));
	}
	
	
	
	MCCFRNode(DecisionNode h){
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
	}
	public void updateRegretSum(int action_index, double regret, int current_iteration) {
		current_iteration_mod_pointer = current_iteration%iteration_mod;
		int next_iteration_mod = (current_iteration+1)%iteration_mod;
		int next_next_iteration_mod = (current_iteration+2)%iteration_mod;
		regretSum[next_iteration_mod][action_index] += regret;
		regretSum[next_next_iteration_mod][action_index] = regretSum[next_iteration_mod][action_index];
	}
	
	public void updateStrategySum(int action_index, int current_iteration) {
		current_iteration_mod_pointer = current_iteration%iteration_mod;
		int next_iteration_mod = (current_iteration+1)%iteration_mod;
		int next_next_iteration_mod = (current_iteration+2)%iteration_mod;
		strategySum[next_iteration_mod][action_index] += strategy[action_index];
		strategySum[next_next_iteration_mod][action_index] = strategySum[next_iteration_mod][action_index];
	}
	
	public double[] getStrategy(int current_iteration) 
	{
		double normalizingSum = 0.0;
		int current_iteration_mod = current_iteration%iteration_mod; 
		for (int a=0; a < total_game_actions; a++)
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
		/*
		//modified version for transforming regrets to strategy
		for (int a=0; a < total_game_actions; a++)
		{
			if (is_valid[a] == false) continue;
			strategy[a] = Math.exp(regretSum[current_iteration_mod_pointer][a]);
			normalizingSum += strategy[a];
		}
		for (int a=0; a < total_game_actions; a++)
		{
			if (is_valid[a] == false) continue;
			strategy[a] /= normalizingSum;
		}
		*/
		return strategy;
	}
	public double[] getAverageStrategy () {
		double[] avgStrategy = new double[total_game_actions];
		double normalizingSum = 0.0;
		int next_iteration_mod_pointer = (current_iteration_mod_pointer+1)%iteration_mod;
		for (int a=0; a < total_game_actions; a++){
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
