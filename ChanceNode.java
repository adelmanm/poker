/*
 *  interface for game chance nodes (states when the outcome is determined by chance and not by player action) 
 */

public interface ChanceNode extends History {
	Outcome sample_outcome(int iteration);
}
