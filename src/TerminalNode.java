/*
 *  interface for game terminal nodes 
 */

public interface TerminalNode extends History {
	double get_utility(int player); //returns the utility/payoff of the terminal game state
}
