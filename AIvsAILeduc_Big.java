import java.util.*;
import java.lang.Math;

public class AIvsAILeduc_Big  
{
	public static final int NUM_GAME_SETTINGS = 10;
	public static final String log_dir_path = "logs/";
	public static final String infoset_filename0 = "infosets.csv";
	public static final String infoset_filename1 = "infosets.csv";
	public static final String game_settings_fliename = "game_settings.csv";
	private static int NUM_PLAYER_CARDS; //number of cards per player
	private static int FLOP_SIZE; // number of community cards revealed in the first flop
	private static TreeMap<String,double[]> strategy_profile0 = new TreeMap<String,double[]>(); //<key, strategy>
	private static TreeMap<String,double[]> strategy_profile1 = new TreeMap<String,double[]>(); //<key, strategy>
	private static final int rounds = 1000000;
	private static String[] settings_name = new String[NUM_GAME_SETTINGS];
	private static int[] settings_value = new int[NUM_GAME_SETTINGS];
	private static double AI0_total_score = 0.0;
	private static double AI1_total_score = 0.0;
	private static final boolean print = false;
	
	public static void main(String[] args) // function Solve in the algorithm.
	{
		read_infosets();
		read_settings();
		for (int i=0; i<rounds; i++){
			if (print == true) {
				System.out.println("Round " + i + ":");
				System.out.println("AI0 is the starting player");
			}
			play_round(0);
			/*
			if (print == true) {
				System.out.println("AI1 is the starting player");
			}
			play_round(1);
			*/
		}
		check_winner();
	}
	
	static void read_infosets()
	{
		CsvFileReader CsvReader = new CsvFileReader();
		String infoset_path0 = log_dir_path + infoset_filename0;
		String infoset_path1 = log_dir_path + infoset_filename1;
		CsvReader.read(infoset_path0,strategy_profile0);
		CsvReader.read(infoset_path1,strategy_profile1);
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
		
	static void play_round(int starting_player)
	{
		HistoryNodeLeduc_Big h = new HistoryNodeLeduc_Big();
		
		while (!h.is_terminal()) {
			int current_round = h.current_round;
			if (current_round>0 && h.decisions[current_round] == null && print == true) {
				reveal_flop(h,current_round);
			}
			
			if (h.is_chance()) {
				do_chance(h);
			}
			if (starting_player == 0) {
				if (h.get_player() == 0) {
					h = (HistoryNodeLeduc_Big)do_AI0_turn(h);
				}
				else if (h.get_player() == 1){
					h = (HistoryNodeLeduc_Big)do_AI1_turn(h);
				}
			}
			else if (starting_player == 1) {
				if (h.get_player() == 0) {
					h = (HistoryNodeLeduc_Big)do_AI1_turn(h);
				}
				else if (h.get_player() == 1){
					h = (HistoryNodeLeduc_Big)do_AI0_turn(h);
				}
			}
		}
		//if we reached here then we are at a terminal state
		calculate_payoff(h, starting_player);	
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
		int cards[][] = ((HistoryNodeLeduc_Big)h).player_cards;
		if (print == true){
			System.out.print("first player received cards ");
			for (int i=0; i<NUM_PLAYER_CARDS; i++) {
				int card = ((HistoryNodeLeduc_Big)h).player_cards[0][i];
				if (i>0) System.out.print(",");
				System.out.print(((HistoryNodeLeduc_Big)h).card_str(card));
			}
			System.out.println(" ");
			System.out.print("second player received cards ");
			for (int i=0; i<NUM_PLAYER_CARDS; i++) {
				int card = ((HistoryNodeLeduc_Big)h).player_cards[1][i];
				if (i>0) System.out.print(",");
				System.out.print(((HistoryNodeLeduc_Big)h).card_str(card));
			}
			System.out.println(" ");
		}
	}

	static History do_AI0_turn(History h)
	{
		Outcome AI0_action = get_action_by_strategy(h,0);
		char AI0_action_char = ((Outcome_Class)AI0_action).getOutcome();
		if (print == true) {
			System.out.println("AI0 plays " + AI0_action_char);
		}
		return h.append(AI0_action);
	}
	
	static History do_AI1_turn(History h)
	{
		Outcome AI1_action = get_action_by_strategy(h,1);
		char AI1_action_char = ((Outcome_Class)AI1_action).getOutcome();
		if (print == true){
			System.out.println("AI1 plays " + AI1_action_char);
		}
		return h.append(AI1_action);
	}
	
	static Outcome get_action_by_strategy (History h, int AI)
	{
		DecisionNode h_decision = (DecisionNode)h;
		String infoset = h_decision.get_information_set();
		double[] strategy;
		if (AI == 0) {
			strategy = strategy_profile0.get(infoset);
		}
		else {
			strategy = strategy_profile1.get(infoset);
		}
		double rnd = Math.random()*0.9999;
		double cum_probability = 0.0;
		for (int a=0 ; a<strategy.length; a++){
			cum_probability += strategy[a];
			//System.out.println("a is " + a + " rnd is" + rnd + " cum_probability is " + cum_probability);
			if (rnd < cum_probability) {
				assert (h_decision.action_valid(a) == true);
				return h_decision.get_decision_outcome(a);
			}
		}
		assert(false); //we're not supposed to reach that
		return (Outcome)null; 
	}
	
	static void calculate_payoff(History h, int starting_player)
	{
		TerminalNode h_terminal = (TerminalNode)h;
		double player0_score = h_terminal.get_utility(0);
		double player1_score = h_terminal.get_utility(1);
		double AI0_score = starting_player == 0 ? player0_score : player1_score;
		double AI1_score = starting_player == 0 ? player1_score : player0_score;
		if (print == true){
			System.out.println("Round finished. AI0 recieved " + AI0_score + " points. AI1 recieved " + AI1_score + " points");
		}
		AI0_total_score += AI0_score;
		AI1_total_score += AI1_score;
	}
	
	static void check_winner()
	{
		System.out.println("Total score: AI0 recieved " + AI0_total_score + " points. AI1 recieved " + AI1_total_score + " points");
		if (AI0_total_score > AI1_total_score) {
			System.out.println("AI0 wins!");
		}
		else if (AI0_total_score < AI1_total_score) {
			System.out.println("AI1 wins!");
		}
		else {
			System.out.println("It's a tie!");
		}
	}
}