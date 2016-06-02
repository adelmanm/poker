/* Implementation of the CFR algorithm - Chance Sampling version */

import java.util.*;
public class TrainCFR_CS {
	private TreeMap<String,CFRNode> nodemap = new TreeMap<String,CFRNode>(); //<key, information set data>
	static CsvFileWriter CsvWriter = new CsvFileWriter();
	
	public double cfr(History h, int player, int iteration, double pi0, double pi1) {
		//Return payoff for terminal states
		if (h.is_terminal()) {
			return ((TerminalNode)h).get_utility(player);// this actually means "get_payoff", since it doesn't include probabilities (algorithm part)
		}
		
		//Sample chance outcome for chance states
		else if (h.is_chance()) {
			Outcome a = ((ChanceNode)h).sample_outcome();
			if (player == 0){
				return cfr(h.append(a), player, iteration, pi0, pi1);
    		}
     		else if(player == 1){
     			return cfr(h.append(a), player, iteration, pi0, pi1);
     		}	
		}
		
		//Get information set node or create if nonexistant
		DecisionNode h_decision = (DecisionNode)h;
		int max_actions = h_decision.max_actions();
		String infoset_key = h_decision.get_information_set();
		CFRNode infoset_node = nodemap.get(infoset_key);
		if (infoset_node == null) {
			infoset_node = new CFRNode(h_decision);
			nodemap.put(infoset_key, infoset_node);
		}
		
		//For each action, recursively call cfr with additional history and probability
		double [] node_utility = new double[max_actions];
		double total_node_utility = 0.0f;
		double [] strategy = infoset_node.getStrategy();
		for (int a=0; a < max_actions; a++){
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
			for (int a=0; a < max_actions; a++){
				if (h_decision.action_valid(a) == false) continue;
				double regret = node_utility[a]-total_node_utility;
				infoset_node.updateTables(player,a,regret,pi0,pi1);
			}
		}
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
	         CFRNode tmpNode=(CFRNode)me.getValue();
	         tmpNode.Print();
	      }
	}
	
	//write strategy profiles to csv
	public void update_strategy_csv(String log_dir_path) {
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
	    while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         CFRNode tmpNode=(CFRNode)me.getValue();
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
	         String filename =  log_dir_path + "infosets.csv";
	         CsvWriter.write(filename, me.getKey().toString());  
	      }
	    CsvWriter.flush_close();
	}
}