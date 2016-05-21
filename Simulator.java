import java.util.*;
public class Simulator  
{
	public static final int NUM_PLAYERS = 2;
	public static final int MAX_ACTIONS = 5;
	
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		int num_iterations;
		if (args.length == 0)  
		{
			num_iterations = 1;
		}
		else 
		{
			num_iterations = Integer.valueOf(args[0]);
		}
		System.out.format("num_iterations is %d\n",num_iterations);
		for (int iteration = 0; iteration < num_iterations; iteration++)
		{
			for (int player=0;player < NUM_PLAYERS;player++)
			{
				HistoryNode h = new HistoryNode(NUM_PLAYERS, MAX_ACTIONS);
				TrainCFR trainer= new TrainCFR();
				float utility = trainer.cfr(h,player,iteration,1.0f,1.0f);
				System.out.print("Itration utility:");
				System.out.println(utility);
				trainer.print();
			}
		}
	}
}