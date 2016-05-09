public interface DecisionNode extends History {
	int num_actions();
	int max_actions();
	boolean action_valid(int action);
	Outcome get_decision_outcome(int outcome_num);
}
