import java.util.*;
public class TrainCFR {
	private TreeMap<String,CFRNode> nodemap = new TreeMap<String,CFRNode>(); //<key, information set data>
	static CsvFileWriter CsvWriter = new CsvFileWriter();
	
	public double cfr(History h, int player, int iteration, double pi0, double pi1) {
		if (h.is_terminal()) {
			return ((TerminalNode)h).get_utility(player);// this actually means "get_payoff", since it doesn't include probabilities (algorithm part)
		}
		else if (h.is_chance()) {
			Outcome a = ((ChanceNode)h).sample_outcome();
			return cfr(h.append(a), player, iteration, pi0, pi1);
		}
		DecisionNode h_decision = (DecisionNode)h;
		int max_actions = h_decision.max_actions();
		String infoset_key = h_decision.get_information_set();
		CFRNode infoset_node = nodemap.get(infoset_key);
		if (infoset_node == null) {
			infoset_node = new CFRNode(h_decision);
			nodemap.put(infoset_key, infoset_node);
		}
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
		if (h_decision.get_player() == player) {
			if (Objects.equals(infoset_key,"2B")) {
				total_node_utility = total_node_utility * 1;
			}
			for (int a=0; a < max_actions; a++){
				if (h_decision.action_valid(a) == false) continue;
				double regret = node_utility[a]-total_node_utility;
				infoset_node.updateTables(player,a,regret,pi0,pi1,iteration);
			}
		}
		return total_node_utility;
	}

	public void print() {
		System.out.println("Final strategy profile:");
		Set set = nodemap.entrySet();
		Iterator i = set.iterator();
	    while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.println(me.getKey() + ": ");
	         CFRNode tmpNode=(CFRNode)me.getValue();
	         tmpNode.Print();
	         tmpNode.WriteCSV(me.getKey().toString());
	      }
	}
}
