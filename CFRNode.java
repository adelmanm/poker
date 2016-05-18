import java.util.Arrays;

public class CFRNode {
	private double[] regretSum;
	private double[] strategy;
	private double[] strategySum;
	private boolean[] is_valid;
	private int max_actions; //number of actions the information set with most actions. used to set arrays length 
	private int num_actions; //number of actions in this information set.
	
	public void Print() {
	    System.out.println(Arrays.toString(getAverageStrategy()));
	}
	
	
	
	CFRNode(DecisionNode h){
		max_actions = h.max_actions();
		num_actions = h.num_actions();
		regretSum = new double[max_actions];
		strategy = new double[max_actions];
		strategySum = new double[max_actions];
		is_valid = new boolean[max_actions];
		for (int a=0; a < max_actions; a++)
		{
			is_valid[a] = h.action_valid(a);
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
	
	public double[] getStrategy() 
	{
		double normalizingSum = 0.0;
		for (int a=0; a < max_actions; a++)
		{
			if (is_valid[a] == false) continue;
			strategy[a] = regretSum[a] > 0 ? regretSum[a] : 0;
			normalizingSum += strategy[a];
		}
		for (int a=0; a < max_actions; a++)
		{
			if (is_valid[a] == false) continue;
			if (normalizingSum > 0)
			{
				strategy[a] /= normalizingSum;
			}
			else 
			{
				strategy[a] = 1.0 /num_actions;
			}
		}
		return strategy;
	}
	public double[] getAverageStrategy () {
		double[] avgStrategy = new double[max_actions];
		double normalizingSum = 0.0;
		for (int a=0; a < max_actions; a++){
			if (is_valid[a] == false) continue;
			normalizingSum += strategySum[a];
		}
		for (int a=0; a < max_actions; a++){
			if (is_valid[a] == false) continue;
			if (normalizingSum > 0) {
				avgStrategy[a] = strategySum[a] / normalizingSum;
			}
			else {
				avgStrategy[a] = 1.0 /num_actions;
			}
		}
		return avgStrategy;
	}
}
