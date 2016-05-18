/*
 *  General interface for game history (a node in the game graph) 
 */

public interface History {
	boolean is_terminal(); //returns true if the current state is terminal
	boolean is_chance(); //returns true if the current state is a chance state
	History append(Outcome a); //returns a possible next history, resulting from adding a specific action/outcome to the current history. 
	String get_information_set(); //returns the information set corresponding to current state
	int get_player(); //returns the number of the player whos turn is at the current state
}
