/*
 *  interface for game decision nodes (states when one of the players can take an action) 
 */

public interface DecisionNode extends History {
	int num_valid_actions(); //number of valid actions at the current state
	int total_game_actions(); //highest number of actions in any state (used to construct arrays length) 
	boolean action_valid(int action); //return true if the action with index "action" is valid for current state 
	Outcome get_decision_outcome(int outcome_num); //returns Outcome corresponding to index outcome_num 
}
