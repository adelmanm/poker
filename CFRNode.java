import java.util.Arrays;

public class CFRNode {
	private double[] regretSum;
	private double[] strategy;
	private double[] strategySum;
	private boolean[] is_valid;
	private int max_actions; //number of actions the information set with most actions. used to set arrays length 
	private int num_actions; //number of actions in this information set.
	private double[][] regretSum_hist;
	private double[][] strategySum_hist;
	private double[][] strategy_hist;
	private double[][] average_strategySum_hist;
	private double[] test;
	static CsvFileWriter CsvWriter = new CsvFileWriter();
	
	public void Print() {
		System.out.println("CFRNode:");
	    System.out.print("regretSum:");
	    System.out.println(Arrays.toString(regretSum));
	    System.out.print("strategy:");
	    System.out.println(Arrays.toString(strategy));
	    System.out.print("strategySum:");
	    System.out.println(Arrays.toString(strategySum));
	    System.out.print("final strategy:");
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
		regretSum_hist = new double[100000][max_actions];
		strategySum_hist = new double[100000][max_actions];
		strategy_hist = new double[100000][max_actions];
		average_strategySum_hist = new double[100000][max_actions];
		
	}
	public void updateTables(int player, int index, double regret, double pi0, double pi1, int iteration) {
		if (player == 0) {
			regretSum[index] += pi1*regret;
			strategySum[index] += pi0*strategy[index];
		}
		else if (player == 1) {
			regretSum[index] += pi0*regret;
			strategySum[index] += pi1*strategy[index];
		}
		if (index == num_actions-1) {
			System.arraycopy(regretSum,0,regretSum_hist[iteration], 0, regretSum.length);
			System.arraycopy(strategySum,0,strategySum_hist[iteration], 0, strategySum.length);
			System.arraycopy(strategy,0,strategy_hist[iteration], 0, strategy.length);
			System.arraycopy(getAverageStrategy(), 0, average_strategySum_hist[iteration], 0, getAverageStrategy().length);
		}
	}
	
	public void WriteCSV(String filename){
	    CsvWriter.write(filename+"_strategySum_hist.csv", strategySum_hist);	
	    CsvWriter.write(filename+"_strategy_hist.csv", strategy_hist);
	    CsvWriter.write(filename+"_regretSum_hist.csv", regretSum_hist);
	    CsvWriter.write(filename+"_average_strategySum_hist.csv", average_strategySum_hist);
	}
	
	public double[] getStrategy() 
	{
		double normalizingSum = 0.0f;
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
				strategy[a] = 1.0f /num_actions;
			}
		}
		return strategy;
	}
	public double[] getAverageStrategy () {
		double[] avgStrategy = new double[max_actions];
		double normalizingSum = 0.0f;
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
				avgStrategy[a] = 1.0f /num_actions;
			}
		}
		return avgStrategy;
	}
}
