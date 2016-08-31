/* Implementation of the MCCFR algorithm - External Sampling with Stochastically Weighted Averaging version */

import java.util.*;
public class TrainMCCFR_trim {
	private TreeMap<String,MCCFRNode_trim> nodemap = new TreeMap<String,MCCFRNode_trim>(); //<key, information set data>
	static CsvFileWriter CsvWriter = new CsvFileWriter();
	
	public double cfr(History h, int player, int iteration, double pi0, double pi1) {
		return mccfr(h, player, iteration);
	}
	public double mccfr(History h, int player, int iteration) {
		
		//Return payoff for terminal states
		if (h.is_terminal()) {
			return ((TerminalNode)h).get_utility(player);// this actually means "get_payoff", since it doesn't include probabilities (algorithm part)
		}
		
		//Sample chance outcome for chance states
		else if (h.is_chance()) {
			Outcome a = ((ChanceNode)h).sample_outcome();
			return mccfr(h.append(a), player, iteration);	
		}
		
		//Get information set node or create if nonexistant
		DecisionNode h_decision = (DecisionNode)h;
		int total_game_actions = h_decision.total_game_actions();
		String infoset_key = h_decision.get_information_set();
		MCCFRNode_trim infoset_node = nodemap.get(infoset_key);
		if (infoset_node == null) {
			infoset_node = new MCCFRNode_trim(h_decision);
			nodemap.put(infoset_key, infoset_node);
		}
		//if the utility is stable, return the mean utility
		if (infoset_node.can_trim(player)) {
			//return infoset_node.get_mean(player);
			return infoset_node.get_mean_est(player);
		}
		
		//statistic to help compare different algorithms
		VisitedNodesCounter.inc();

		//get strategy through regret matching
		double [] strategy = infoset_node.getStrategy(iteration);
		
		//for the learning player, go over all actions and update regret sum
		if (h_decision.get_player() == player) {
			double [] node_utility = new double[total_game_actions];
			double total_node_utility = 0.0;
			for (int a=0; a < total_game_actions; a++){
				if (h_decision.action_valid(a) == false) continue;
				node_utility[a] = mccfr(h_decision.append(h_decision.get_decision_outcome(a)), player, iteration);
				total_node_utility += strategy[a]*node_utility[a];
			}
			for (int a=0; a < total_game_actions; a++){
				if (h_decision.action_valid(a) == false) continue;
				double regret = node_utility[a]-total_node_utility;
				infoset_node.updateRegretSum(a,regret,iteration);
			}
			infoset_node.updateUtility(total_node_utility,player);
			return total_node_utility;
		}
		//for the opponent, sample an action according to his strategy profile and update strategy sum
		else{
			Outcome sampled_opponent_action = get_action_by_strategy(h,strategy);
			double u = mccfr(h.append(sampled_opponent_action), player, iteration);
			for (int a=0; a < total_game_actions; a++){
				if (h_decision.action_valid(a) == false) continue;
				infoset_node.updateStrategySum(a,iteration);
			}
			return u;
		}
	}
	
	//sample player action according to a strategy profile
	static Outcome get_action_by_strategy (History h, double[] strategy)
	{
		DecisionNode h_decision = (DecisionNode)h;
		double rnd = Math.random();
		double cum_probability = 0.0;
		for (int a=0 ; a<strategy.length; a++){
			cum_probability += strategy[a];
			//System.out.println("a is " + a + " rnd is" + rnd + " cum_probability is " + cum_probability);
			if (rnd < cum_probability) {
				assert (h_decision.action_valid(a));
				return h_decision.get_decision_outcome(a);
			}
		}
		assert(false); //we're not supposed to reach that
		return (Outcome)null; 
	}
	
	//print strategy profile
	public void print() {
		System.out.println("Final strategy profile:");
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
	    while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.print(me.getKey() + ": ");
	         MCCFRNode_trim tmpNode=(MCCFRNode_trim)me.getValue();
	         tmpNode.Print();
	      }
	}
	
	//write strategy profiles to csv
	public void update_strategy_csv(String log_dir_path) {
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
	    while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         MCCFRNode_trim tmpNode=(MCCFRNode_trim)me.getValue();
	         String filename =  log_dir_path + me.getKey() + "_strategy.csv";
	         double strategy[] = tmpNode.getAverageStrategy();
	         CsvWriter.write(filename, strategy);
	      }
	}
	
	//create a csv containing the infosets name
	public void create_infoset_csv(String log_dir_path) {
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	         MCCFRNode_trim tmpNode = (MCCFRNode_trim)me.getValue();
	         double strategy[] = tmpNode.getAverageStrategy();
	         String filename =  log_dir_path + "infosets.csv";
	         CsvWriter.write(filename, me.getKey().toString(), strategy);
	      }
	    CsvWriter.flush_close();
	}
}
