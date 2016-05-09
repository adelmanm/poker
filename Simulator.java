import java.util.*;
public class Simulator  
{
	public static final int NUM_PLAYERS = 2;
	public static final int MAX_ACTIONS = 2;
	
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		int num_iterations;
		if (args.length == 0) 
		{
			num_iterations = 100000;
		}
		else 
		{
			num_iterations = Integer.valueOf(args[0]);
		}
		System.out.format("num_iterations is %d\n",num_iterations);
		TrainCFR trainer= new TrainCFR();
		double utilHist[][] = new double[NUM_PLAYERS][num_iterations];
		double utility;
		for (int iteration = 0; iteration < num_iterations; iteration++)
		{
			for (int player=0;player < NUM_PLAYERS;player++)
			{
				HistoryNode h = new HistoryNode(NUM_PLAYERS, MAX_ACTIONS);
				utility = trainer.cfr(h,player,iteration,1.0f,1.0f);
				//update average utility history
				if (iteration == 0) {
					utilHist[player][iteration] = utility;
				}
				else {
					utilHist[player][iteration] = (utilHist[player][iteration-1] * (iteration - 1) + utility) / iteration;
				}
				//System.out.print("Itration utility:");
				//System.out.println(utility);
				//trainer.print();
			}
		}
		trainer.print();
		System.out.print("player 0 utility:");
		System.out.println(utilHist[0][num_iterations-1]);
	    System.out.print("player 1 utility:");
	    System.out.println(utilHist[1][num_iterations-1]);
	    CsvFileWriter CsvWriter = new CsvFileWriter();
	    CsvWriter.write("util_hist.csv", utilHist);
	}
}