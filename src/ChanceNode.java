/*
 *  interface for game chance nodes (states when the outcome is determined by chance and not by player action) 
 */

public interface ChanceNode extends History {
	Outcome sample_outcome();
	int num_chance_outcomes(); //number of possible chance outcomes at the current state
	Outcome get_chance_outcome(int outcome_num); //returns Outcome corresponding to index outcome_num
	double get_chance_outcome_probability(int outcome_num); //returns the probability for the corresponding chance outcome 
}
