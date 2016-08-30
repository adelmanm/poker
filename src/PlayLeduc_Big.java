import java.util.*;
import java.io.File;
import java.lang.Math;

public class PlayLeduc_Big  
{
	public static final int NUM_GAME_SETTINGS = 10;
	public static final String log_dir_path = "logs/";
	public static final String infoset_filename = "infosets.csv";
	public static final String game_settings_fliename = "game_settings.csv";
	private static TreeMap<String,double[]> strategy_profile = new TreeMap<String,double[]>(); //<key, strategy>
	private static int NUM_PLAYER_CARDS; //number of cards per player
	private static int FLOP_SIZE; // number of community cards revealed in the first flop
	private static int player;
	private static int rounds;
	private static String[] settings_name = new String[NUM_GAME_SETTINGS];
	private static int[] settings_value = new int[NUM_GAME_SETTINGS];
	private static double player_total_score = 0.0;
	private static double computer_total_score = 0.0;
	private static Scanner reader;
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		reader = new Scanner(System.in);  // Reading from System.in
		read_infosets();
		read_settings();
		player = get_player();
		rounds = get_rounds();
		for (int i=1; i<=rounds; i++){
			System.out.println("Round " + i + ":");
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
	
	static void read_settings()
	{
		CsvFileReader CsvReader = new CsvFileReader();
		String setting_path = log_dir_path + game_settings_fliename;
		CsvReader.read_game_settings(setting_path, settings_name, settings_value);
		CsvReader.close();
		System.out.println("Game settings:");
		for (int i = 0; i< NUM_GAME_SETTINGS; i++){
			System.out.println(settings_name[i] + ": " + String.valueOf(settings_value[i]));
		}
		assert(settings_name[4].equals("num_player_cards"));
		NUM_PLAYER_CARDS = settings_value[4];
		assert(settings_name[7].equals("flop_size"));
		FLOP_SIZE = settings_value[7];
		System.out.println(" ");
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
		while (rounds_number_int <= 0) {
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
		HistoryNodeLeduc_Big h = new HistoryNodeLeduc_Big();
		while (!h.is_terminal()) {
			int current_round = h.current_round;
			if (current_round>0 && h.decisions[current_round] == null) {
				reveal_flop(h,current_round);
			}
			if (h.is_chance()) {
				do_chance(h);
			}
			else if (h.get_player() == player) {
				h = (HistoryNodeLeduc_Big)do_player_turn(h);
			}
			else if (h.get_player() == 1-player){
				h = (HistoryNodeLeduc_Big)do_computer_turn(h);
			}
		}
		//if we reached here then we are at a terminal state
		calculate_payoff(h);	
	}
	
	static void reveal_flop(HistoryNodeLeduc_Big h, int current_round)
	{
		if (current_round == 1) {
			for (int i=0; i<FLOP_SIZE; i++){
				System.out.println("community_card number " + String.valueOf(i+1)+ " is " + h.card_str(h.community_cards[i]));
			}
		}
		else {
			System.out.println("community_card number " + String.valueOf(current_round+1)+ " is " + h.card_str(h.community_cards[FLOP_SIZE + current_round-2]));
		}
	}
	
	static void do_chance(History h) 
	{
		assert(h.is_chance());
		ChanceNode h_chance = (ChanceNode)h;
		h_chance.sample_outcome();
		System.out.print("Player " + player + " your cards are ");
		for (int i=0; i<NUM_PLAYER_CARDS; i++) {
			int card = ((HistoryNodeLeduc_Big)h).player_cards[player][i];
			if (i>0) System.out.print(",");
			System.out.print(((HistoryNodeLeduc_Big)h).card_str(card));
		}
		System.out.println(" ");
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
		double cum_probability = 0.0;
		for (int a=0 ; a<strategy.length; a++){
			cum_probability += strategy[a];
			//System.out.println("a is " + a + " rnd is" + rnd + " cum_probability is " + cum_probability);
			if (rnd < cum_probability) {
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
		if (!((HistoryNodeLeduc_Big)h).decisions[((HistoryNodeLeduc_Big)h).current_round].endsWith("F")) {
			System.out.print("Computer cards are ");
			for (int i=0; i<NUM_PLAYER_CARDS; i++) {
				int card = ((HistoryNodeLeduc_Big)h).player_cards[1-player][i];
				if (i>0) System.out.print(",");
				System.out.print(((HistoryNodeLeduc_Big)h).card_str(card));
			}
			System.out.println(" ");
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