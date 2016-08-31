/* Implementation of the CFR algorithm - Vanilla version */
import java.util.*;
public class TrainCFR_Vanilla_trim_old {
	private TreeMap<String,CFRNode_trim_old> nodemap = new TreeMap<String,CFRNode_trim_old>(); //<key, information set data>
	static CsvFileWriter CsvWriter = new CsvFileWriter();
	
	public double cfr(History h, int player, int iteration, double pi0, double pi1) {
		
		//Return payoff for terminal states
		if (h.is_terminal()) {
			return ((TerminalNode)h).get_utility(player);// this actually means "get_payoff", since it doesn't include probabilities (algorithm part)
		}
		
		//Go over all chance outcomes for chance states
		else if (h.is_chance()) {
			ChanceNode h_chance = (ChanceNode)h;
			int num_outcomes = h_chance.num_chance_outcomes();
			double util = 0.0;
			for (int i=0;i<num_outcomes;i++){
				Outcome a = h_chance.get_chance_outcome(i);
				double prob =h_chance.get_chance_outcome_probability(i);
				if (player == 0)
					 util += prob*cfr(h.append(a), player, iteration, pi0, pi1*prob);
				else if (player == 1)
					util += prob*cfr(h.append(a), player, iteration, pi0*prob, pi1);
			}
			return util;		
		}
		
		//Get information set node or create if nonexistant
		DecisionNode h_decision = (DecisionNode)h;
		int total_game_actions = h_decision.total_game_actions();
		String infoset_key = h_decision.get_information_set();
		CFRNode_trim_old infoset_node = nodemap.get(infoset_key);
		if (infoset_node == null) {
			infoset_node = new CFRNode_trim_old(h_decision);
			nodemap.put(infoset_key, infoset_node);
		}
		
		//if the utility is stable, return the mean utility
		if (infoset_node.can_trim(player)) {
			//return infoset_node.get_mean(player);
			return infoset_node.get_mean_est(player);
		}
		
		//statistic to help compare different algorithms
		VisitedNodesCounter.inc();
		
		//For each action, recursively call cfr with additional history and probability
		double [] node_utility = new double[total_game_actions];
		double total_node_utility = 0.0;
		double [] strategy = infoset_node.getStrategy(iteration);
		for (int a=0; a < total_game_actions; a++){
			if (h_decision.action_valid(a) == false) continue;
			if (h_decision.get_player() == 0) {
				node_utility[a] = cfr(h_decision.append(h_decision.get_decision_outcome(a)), player, iteration, strategy[a]*pi0, pi1);
			}
			else if (h_decision.get_player() == 1) {
				node_utility[a] = cfr(h_decision.append(h_decision.get_decision_outcome(a)), player, iteration, pi0, strategy[a]*pi1);
			}
			total_node_utility += strategy[a]*node_utility[a];
		}
		
		//For each action, compute and accumulate counterfactual regrets
		if (h_decision.get_player() == player) {
			for (int a=0; a < total_game_actions; a++){
				if (h_decision.action_valid(a) == false) continue;
				double regret = node_utility[a]-total_node_utility;
				infoset_node.updateTables(player,a,regret,pi0,pi1,iteration);
			}
		}
		
		infoset_node.updateUtility(total_node_utility,player);
		return total_node_utility;
	}

	//print strategy profile
	public void print() {
		System.out.println("Final strategy profile:");
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
	    while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.print(me.getKey() + ": ");
	         CFRNode_trim_old tmpNode=(CFRNode_trim_old)me.getValue();
	         tmpNode.Print();
	      }
	}
	
	//write strategy profiles to csv
	public void update_strategy_csv(String log_dir_path) {
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
	    while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         CFRNode_trim_old tmpNode=(CFRNode_trim_old)me.getValue();
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
	         CFRNode_trim_old tmpNode = (CFRNode_trim_old)me.getValue();
	         double strategy[] = tmpNode.getAverageStrategy();
	         String filename =  log_dir_path + "infosets.csv";
	         CsvWriter.write(filename, me.getKey().toString(), strategy);  
	      }
	    CsvWriter.flush();
	}
}
