/*
 *  Interface for Outcome. an Outcome can be the result of a chance event or a player action.
 *  Outcomes can be appended to History (game state) to produce a new future state. 
 */

public interface Outcome {
	int to_int(); //Outcome should be convertible to an array index 
}
