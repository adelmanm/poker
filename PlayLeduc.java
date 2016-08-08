import java.util.*;
import java.io.File;
import java.lang.Math;

public class PlayLeduc  
{
	public static final int NUM_PLAYERS = 2;
	public static final int TOTAL_GAME_ACTIONS = 5;
	public static final String log_dir_path = "logs/";
	public static final String infoset_filename = "infosets.csv";
	private static TreeMap<String,double[]> strategy_profile = new TreeMap<String,double[]>(); //<key, strategy>
	private static int player;
	private static int rounds;
	private static double player_total_score = 0.0;
	private static double computer_total_score = 0.0;
	private static Scanner reader;
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		reader = new Scanner(System.in);  // Reading from System.in
		read_infosets();
		player = get_player();
		rounds = get_rounds();
		for (int i=0; i<rounds; i++){
			play_round();
		}
		check_winner();
		reader.close();
	}
	
	static void read_infosets()
	{
		CsvFileReader CsvReader = new CsvFileReader();
		String infoset_path = log_dir_path + infoset_filename;
		CsvReader.read(infoset_path,strategy_profile);
		CsvReader.close();
	}
	
	static int get_player()
	{
		System.out.println("Which player do you want to play (0 or 1)? ");
		String player_number = reader.next(); 
		while (!player_number.equals("0") && !player_number.equals("1")) {
			System.out.println("Invalid player number. Please try again: ");
			player_number = reader.next(); 
		}
		return Integer.parseInt(player_number);
	}
	
	static int get_rounds()
	{
		System.out.println("Enter number of rounds to play: ");
		String rounds_number_str = reader.next(); 
		int rounds_number_int;
		try {
			rounds_number_int = Integer.parseInt(rounds_number_str);
		}
		catch (Exception e) {
			rounds_number_int = -1;
		}
		while (rounds_number_int < 0) {
			System.out.println("Invalid rounds number. Please try again: ");
			rounds_number_str = reader.next(); // Scans the next token of the input as an int.
			try {
				rounds_number_int = Integer.parseInt(rounds_number_str);
			}
			catch (Exception e){
				rounds_number_int = -1;
			}
		}
		return rounds_number_int;
	}
	
	static void play_round()
	{
		HistoryNodeLeduc h = new HistoryNodeLeduc(NUM_PLAYERS, TOTAL_GAME_ACTIONS);
		boolean flop_revealed = false;
		while (!h.is_terminal()) {
			if (flop_revealed == false && h.post_flop()) {
				reveal_flop(h);
				flop_revealed = true;
			}
			if (h.is_chance()) {
				do_chance(h);
			}
			else if (h.get_player() == player) {
				h = (HistoryNodeLeduc)do_player_turn(h);
			}
			else if (h.get_player() == 1-player){
				h = (HistoryNodeLeduc)do_computer_turn(h);
			}
		}
		//if we reached here then we are at a terminal state
		calculate_payoff(h);	
	}
	
	static void reveal_flop(HistoryNodeLeduc h)
	{
		System.out.println("Flop is revealed as " + h.cards[2]);
	}
	
	static void do_chance(History h) 
	{
		assert(h.is_chance());
		ChanceNode h_chance = (ChanceNode)h;
		h_chance.sample_outcome();
		int card = ((HistoryNodeLeduc)h).cards[player];
		System.out.println("Player " + player + " your card is " + card);
	}
	
	static History do_player_turn(History h)
	{
		DecisionNode h_decision = (DecisionNode)h;
		System.out.print("Player " + player + " it's your turn. Please enter action (");
		boolean comma = false;
		for (int a=0; a < h_decision.total_game_actions(); a++){
			Outcome possible_action = h_decision.get_decision_outcome(a);
			char possible_action_char = ((Outcome_Class)possible_action).getOutcome();
			//System.out.println("possible action index is " + possible_action.to_int() + " and char is " + possible_action_char);
			if (h_decision.action_valid(a)) {
				if (comma) {
					System.out.print(",");
				}
				else {
					comma = true;
				}
				System.out.print(possible_action_char);
			}
		}
		System.out.println(")");
		while(true){
			String action = reader.next();
			//System.out.println("action is "+ action);
			for (int a=0; a < h_decision.total_game_actions(); a++){
				Outcome possible_action = h_decision.get_decision_outcome(a);
				char possible_action_char = ((Outcome_Class)possible_action).getOutcome();
				//System.out.println("possible action index is " + possible_action.to_int() + " and char is " + possible_action_char);
				if (action.equals(String.valueOf(possible_action_char)) && h_decision.action_valid(a)) {
					return h.append(possible_action);
				}
			}
			System.out.println("Illegal action, please try again.");
		}	
	}
	
	static History do_computer_turn(History h)
	{
		Outcome computer_action = get_action_by_strategy(h);
		char computer_action_char = ((Outcome_Class)computer_action).getOutcome();
		System.out.println("Computer is player " + (1-player) + " and plays " + computer_action_char);
		return h.append(computer_action);
	}
	
	static Outcome get_action_by_strategy (History h)
	{
		DecisionNode h_decision = (DecisionNode)h;
		String infoset = h_decision.get_information_set();
		double[] strategy = strategy_profile.get(infoset);
		double rnd = Math.random();
		double cum_strategy = 0.0;
		for (int a=0 ; a<strategy.length; a++){
			cum_strategy += strategy[a];
			//System.out.println("a is " + a + " rnd is" + rnd + " cum_strategy is " + cum_strategy);
			if (rnd < cum_strategy) {
				assert (h_decision.action_valid(a));
				return h_decision.get_decision_outcome(a);
			}
		}
		assert(false); //we're not supposed to reach that
		return (Outcome)null; 
	}
	
	static void calculate_payoff(History h)
	{
		TerminalNode h_terminal = (TerminalNode)h;
		double player_score = h_terminal.get_utility(player);
		double computer_score = h_terminal.get_utility(1-player);
		if (!((HistoryNodeLeduc)h).decisions.endsWith("F")) {
			System.out.println("Computer card is " + ((HistoryNodeLeduc)h).cards[1-player]);
		}
		System.out.println("Round finished. Player recieved " + player_score + " points. Computer (player " + (1-player) + ") recieved " + computer_score + " points");
		player_total_score += player_score;
		computer_total_score += computer_score;
	}
	
	static void check_winner()
	{
		System.out.println("Total score: Player recieved " + player_total_score + " points. Computer (player " + (1-player) + ") recieved " + computer_total_score + " points");
		if (player_total_score > computer_total_score) {
			System.out.println("Player wins!");
		}
		else if (player_total_score < computer_total_score) {
			System.out.println("Computer wins!");
		}
		else {
			System.out.println("It's a tie!");
		}
	}
}