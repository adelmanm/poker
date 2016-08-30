import java.util.Arrays;
import java.util.Random;

public class HistoryNodeLeduc_Big implements History, ChanceNode, DecisionNode, TerminalNode 
{	
	public static final int NUM_GAME_SETTINGS = 10;
	public static final String log_dir_path = "logs/";
	public static final String game_settings_fliename = "game_settings.csv";
	public static final int NUM_PLAYERS ;
	public static final int TOTAL_GAME_ACTIONS ;
	public static final int ROUNDS; // number of betting rounds, including the first blind one.
	public static final int DECK_SIZE; // total number of cards  
	public static final int NUM_PLAYER_CARDS; //number of cards per player
	public static final int NUM_COMMUNITY_CARDS; //number of community cards.
	public static final int NUM_SUITS; //number of different card suits (cards with the same number)
	public static final int NUM_CARD_NUMBERS; //number of different card numbers
	public static final int HAND_SIZE; // size of the poker hand (can be composed from player and community cards) 
	public static final int FLOP_SIZE; // number of community cards revealed in the first flop
	public static final int STRAIGHT_SIZE = 5;
	public static final int ANTE; //the money each player pays at the beginning of the game
	public static final int[] BET_SUM; // length = rounds. amount of bet per round.
	// memory - we need to save cards and decisions.
	int[][] player_cards; // 2 X NUM_PLAYER_CARDS 
	int[] community_cards; // NUM_COMMUNITY_CARDS
	int current_round; //pointer for the current round
	public static final int[] allCards; //holds the entire deck
	
	String[] decisions; // length=rounds, maximal string length=4.  
	
	static {
		String[] settings_name = new String[NUM_GAME_SETTINGS];
		int[] settings_value = new int[NUM_GAME_SETTINGS];
		CsvFileReader CsvReader = new CsvFileReader();
		CsvReader.read_game_settings(log_dir_path + game_settings_fliename, settings_name, settings_value);
		assert(settings_name[0].equals("num_players"));
		NUM_PLAYERS = settings_value[0];
		assert(settings_name[1].equals("total_game_actions"));
		TOTAL_GAME_ACTIONS = settings_value[1];
		assert(settings_name[2].equals("rounds"));
		ROUNDS = settings_value[2];
		assert(settings_name[3].equals("deck_size"));
		DECK_SIZE = settings_value[3];
		assert(settings_name[4].equals("num_player_cards"));
		NUM_PLAYER_CARDS = settings_value[4];
		assert(settings_name[5].equals("num_suits"));
		NUM_SUITS = settings_value[5];
		assert(settings_name[6].equals("hand_size"));
		HAND_SIZE = settings_value[6];
		assert(settings_name[7].equals("flop_size"));
		FLOP_SIZE = settings_value[7];
		assert(settings_name[8].equals("ante"));
		ANTE = settings_value[8];
		assert(settings_name[9].equals("bet_sum"));
		//BET_SUM is filled later by CsvReader
		
		NUM_COMMUNITY_CARDS = FLOP_SIZE + (ROUNDS-2); //no cards in the first round, FLOP_SIZE in the 2nd and one in each subsequent round.
		assert(NUM_PLAYERS == 2);
		assert(DECK_SIZE >= NUM_PLAYERS*NUM_PLAYER_CARDS + NUM_COMMUNITY_CARDS);
		assert(NUM_SUITS >= 1 && NUM_SUITS <= 4); //not strictly needed, but this way the winning logic will make more sense
		assert (DECK_SIZE % NUM_SUITS == 0);
		assert(HAND_SIZE <= NUM_PLAYER_CARDS+NUM_COMMUNITY_CARDS);
		NUM_CARD_NUMBERS = DECK_SIZE / NUM_SUITS;
		BET_SUM=new int[ROUNDS]; //new bet each round.
		CsvReader.read_bet_sum(log_dir_path + game_settings_fliename, BET_SUM, ROUNDS);
		allCards = new int[DECK_SIZE];
		create_deck();
	}
	
	
	HistoryNodeLeduc_Big()
	{ 	
		// memory - we need to save cards and decisions.
		player_cards= new int[2][NUM_PLAYER_CARDS]; // should be initialized to 0.
		community_cards = new int[NUM_COMMUNITY_CARDS];
		decisions=new String[ROUNDS]; // should be initialized to NULL.
		current_round = 0;
	}
	 

	
	@Override 
	public double get_utility(int player) // Terminal node // this actually means "get_payoff", since it doesn't include probabilities (algorithm part)
	{
		assert(is_terminal());  
		double[] player_bets = new double[NUM_PLAYERS];
		double pot = 0.0;
		double pay_off=0.0;
		pot = get_player_bets(player_bets);
		int winner = who_won(); // returns -1 if no one won.
		if (winner==-1) { //there's a tie, players split the pot so no one profits
			return 0.0;
		}
		else //there's a winner
		{
			pay_off = player==winner ? pot - player_bets[player] : -player_bets[player];
			return pay_off;
		}
	}

	private double get_player_bets(double[] player_bets)
	{
		double pot = 0.0;
		for (int i=0; i< NUM_PLAYERS; i++){ 
			player_bets[i] = ANTE; //update with the initial bets
			pot += ANTE;
		}
		int betting_player;
		for (int i=0; i< ROUNDS; i++)
		{
			char curr_char=' ';
			if (decisions[i]!=null)
			{
				for (int j=0; j<decisions[i].length(); j++)
				{
					betting_player = j%2 ;
					curr_char= decisions[i].charAt(j);
					switch (curr_char)
					{
						case 'c':
							break;
						case 'b': //fall through
						case 'C':
							player_bets[betting_player] += BET_SUM[i]; 
							pot += BET_SUM[i];
							break;
						case 'R': 
							player_bets[betting_player] += 2*BET_SUM[i];
							pot += 2*BET_SUM[i];
							break;
						case 'F':
							return pot;
						default : 
							assert(false);
							break;
					}	
				}
			}
		}
		return pot;
	}
	private int who_won()
	{
		assert (NUM_PLAYERS == 2); //this logic assumes 2 players only
		int[][] player_histogram = new int[NUM_PLAYERS][NUM_CARD_NUMBERS];
		int remaining_cards_to_hand = HAND_SIZE;
		int winner;
		
		// Check if fold ended the game
		winner = check_fold();
		if (winner == 0 || winner == 1) return winner;
		
		fill_player_histograms(player_histogram);
		
		//check for four of a kind
		if (NUM_SUITS >= 4 && remaining_cards_to_hand >= 4) {
			winner = check_four_of_kind(player_histogram);
			if (winner == 0 || winner == 1) return winner;
			else if (winner == -1) remaining_cards_to_hand -= 4;
		}
		
		//check for full house
		if (NUM_SUITS >= 3 && remaining_cards_to_hand >= 5) {
			winner = check_full_house(player_histogram);
			if (winner == 0 || winner == 1) return winner;
			else if (winner == -1) remaining_cards_to_hand -= 5;
		}
		
		//check for straight
		if (NUM_CARD_NUMBERS >= 5 && remaining_cards_to_hand >= 5) {
			winner = check_straight(player_histogram);
			if (winner == 0 || winner == 1) return winner;
			else if (winner == -1) remaining_cards_to_hand -= 5;
		}
		
		
		//check for three of a kind
		if (NUM_SUITS >= 3 && remaining_cards_to_hand >= 3) {
			winner = check_three_of_kind(player_histogram);
			if (winner == 0 || winner == 1) return winner;
			else if (winner == -1) remaining_cards_to_hand -= 3;
		}
		
		//check for two pairs
		if (NUM_SUITS >= 2 && remaining_cards_to_hand >= 4) {
			winner = check_two_pairs(player_histogram);
			if (winner == 0 || winner == 1) return winner;
			else if (winner == -1) remaining_cards_to_hand -= 4;
		}
		
		//check for pair
		if (NUM_SUITS >= 2 && remaining_cards_to_hand >= 2) {
			winner = check_pair(player_histogram);
			if (winner == 0 || winner == 1) return winner;
			else if (winner == -1) remaining_cards_to_hand -= 2;
		}
		
		//check for high card
		if (remaining_cards_to_hand >= 1){
			winner = check_high_card(player_histogram,remaining_cards_to_hand);
		}
		return winner;
	}
	
	/* go over player hands and flops and fill the histogram 
	 */
	private void fill_player_histograms(int[][] player_histogram)
	{
		int base_card = NUM_CARD_NUMBERS < 5 ? 11 : 2;
		for (int i=0; i<NUM_PLAYERS; i++){
			for (int j=0; j<NUM_COMMUNITY_CARDS; j++) {
				player_histogram[i][community_cards[j]-base_card]++;
			}
			for (int j=0; j<NUM_PLAYER_CARDS; j++) {
				player_histogram[i][player_cards[i][j]-base_card]++;
			}
		}
	}
	
	/*if one player played FOLD then this function returns true
	 * and winner will be 0 or 1 (depends if player 0 wins
	 * or player 1 wins).
	 * otherwise (no fold), returns -2 
	 */
	private int check_fold()
	{
		if (decisions[current_round].endsWith("F")) {
			return decisions[current_round].length() % 2;
		}
		else
			return -2;
	}
	
	/* returns 0 if player 0 has higher pairs
	 * returns 1 if player 1 has higher pairs
	 * returns -1 if both players have highest pair
	 * returns -2 if no player has pair 
	 */
	private int check_four_of_kind(int [][] player_histogram)
	{
		return check_tuple(player_histogram, 4);
	}
	
	/* returns 0 if player 0 has higher full house
	 * returns 1 if player 1 has higher full house
	 * returns -1 if both players have highest full house
	 * returns -2 if no player has full house 
	 */
	private int check_full_house(int [][] player_histogram)
	{
		assert(NUM_SUITS <= 4); //otherwise full house logic fails
		int[] highest_three = new int[NUM_PLAYERS];
		int[] highest_two = new int[NUM_PLAYERS];
		for (int i=0; i<NUM_PLAYERS; i++){
			highest_three[i] = -1;
			highest_two[i] = -1;
		}
		for (int i=NUM_CARD_NUMBERS-1; i>=0; i--) {
			if (highest_three[0] != -1 && highest_two[0] != -1) break;
			if (highest_three[1] != -1 && highest_two[1] != -1) break;
			for (int j=0; j<NUM_PLAYERS; j++){
				if (highest_three[j] == -1 && player_histogram[j][i] >= 3) {
					highest_three[j] = i;
				}
				else if (highest_two[j] == -1 && player_histogram[j][i] >= 2){
					highest_two[j] = i;
				}
			}
		}
		if (highest_three[0] != -1 &&
				highest_two[0] != -1 &&	
				highest_three[0] == highest_three[1] && 
				highest_two[0] == highest_two[1]) {//both players have same full house
			for (int i=0; i<NUM_PLAYERS; i++) {
				player_histogram[i][highest_three[i]] -= 3;
				player_histogram[i][highest_two[i]] -= 2;
			}
			return -1;
		}
		else if ((highest_three[0] > highest_three[1] && highest_two[0] != -1) ||
				(highest_three[0] == highest_three[1] && highest_two[0] > highest_two[1])) { //player 0 has stronger full house
			return 0;
		}
		else if ((highest_three[1] > highest_three[0] && highest_two[1] != -1) ||
				(highest_three[1] == highest_three[0] && highest_two[1] > highest_two[0])) { //player 1 has stronger full house
			return 1;
		}
		else { //no player has full house
			return -2;
		}		
	}
	
	/* returns 0 if player 0 has higher straight
	 * returns 1 if player 1 has higher straight
	 * returns -1 if both players have highest straights
	 * returns -2 if no player has straight 
	 */
	private int check_straight(int [][] player_histogram)
	{
		boolean[] has_straight = new boolean[NUM_PLAYERS];
		assert (NUM_CARD_NUMBERS >= 5);
		for (int i=NUM_CARD_NUMBERS-1; i>=4; i--) {
			for (int j=0; j<NUM_PLAYERS; j++){
				has_straight[j] = true;
				for (int k=0; k<STRAIGHT_SIZE; k++) {
					if (player_histogram[j][i-k] == 0) {
						has_straight[j] = false;
					}
				}
			}
			if (has_straight[0] == true && has_straight[1] == true) { //both players have high straight
				for (int j=0; j<NUM_PLAYERS; j++) {
					for (int k=0; k < STRAIGHT_SIZE; k++) {
						player_histogram[j][i-k]--;
					}
				}
				return -1;
			}
			else if (has_straight[0] == true && has_straight[1] == false) { //player 0 has higher straight
				return 0;
			}
			else if (has_straight[1] == true && has_straight[0] == false) { //player 1 has higher straight
				return 1;
			}
		}
		return -2; //no player has straight
	}
	
	/* returns 0 if player 0 has higher pairs
	 * returns 1 if player 1 has higher pairs
	 * returns -1 if both players have highest pair
	 * returns -2 if no player has pair 
	 */
	private int check_three_of_kind(int [][] player_histogram)
	{
		return check_tuple(player_histogram, 3);
	}
	
	/* returns 0 if player 0 has higher two pairs
	 * returns 1 if player 1 has higher two pairs
	 * returns -1 if both players have highest two pairs
	 * returns -2 if no player has two pairs 
	 */
	private int check_two_pairs(int [][] player_histogram)
	{
		assert(NUM_SUITS >= 2);
		//we assume that two pairs are in different numbers (otherwise it should be four of a kind)
		int[] high_pair = new int[NUM_PLAYERS];
		int[] low_pair = new int[NUM_PLAYERS];
		for (int i=0; i<NUM_PLAYERS; i++){
			high_pair[i] = -1;
			low_pair[i] = -1;
		}
		for (int i=NUM_CARD_NUMBERS-1; i>=0; i--) {
			if (high_pair[0] != -1 && low_pair[0] != -1) break;
			if (high_pair[1] != -1 && low_pair[1] != -1) break;
			for (int j=0; j<NUM_PLAYERS; j++){
				if (high_pair[j] == -1 && player_histogram[j][i] >= 2) {
					high_pair[j] = i;
				}
				else if (low_pair[j] == -1 && player_histogram[j][i] >= 2){
					low_pair[j] = i;
				}
			}
		}
		if (high_pair[0] != -1 &&
				low_pair[0] != -1 &&	
				high_pair[0] == high_pair[1] && 
				low_pair[0] == low_pair[1]) {//both players have same two pairs
			for (int i=0; i<NUM_PLAYERS; i++) {
				player_histogram[i][high_pair[i]] -= 2;
				player_histogram[i][low_pair[i]] -= 2;
			}
			return -1;
		}
		else if ((high_pair[0] > high_pair[1] && low_pair[0] != -1) ||
				(high_pair[0] == high_pair[1] && low_pair[0] > low_pair[1])) { //player 0 has stronger full house
			return 0;
		}
		else if ((high_pair[1] > high_pair[0] && low_pair[1] != -1) ||
				(high_pair[1] == high_pair[0] && low_pair[1] > low_pair[0])) { //player 1 has stronger full house
			return 1;
		}
		else { //no player has full house
			return -2;
		}		
	}
	
	/* returns 0 if player 0 has higher pairs
	 * returns 1 if player 1 has higher pairs
	 * returns -1 if both players have highest pair
	 * returns -2 if no player has pair 
	 */
	private int check_pair(int [][] player_histogram)
	{
		return check_tuple(player_histogram, 2);
	}
	
	/* returns 0 if player 0 has higher single cards
	 * returns 1 if player 1 has higher single cards
	 * otherwise, (no higher card for both players) returns -1  
	 */
	private int check_high_card(int[][] player_histogram, int remaining_cards)
	{
		int winner = -1;
		for (int i=0; i< remaining_cards; i++){
			winner = check_tuple(player_histogram, 1);
			if (winner != -1) return winner;
		}
		return winner;
	}
	
	/* returns 0 if player 0 has higher tuples
	 * returns 1 if player 1 has higher tuples
	 * returns -1 if both players have highest tuple
	 * returns -2 if no player has highest tuple 
	 */
	private int check_tuple(int [][] player_histogram, int tuple)
	{
		assert (tuple >= 1);
		for (int i=NUM_CARD_NUMBERS-1; i>=0; i--) {
			if (player_histogram[0][i] == tuple && player_histogram[1][i] == tuple) { //both players have highest tuple
				player_histogram[0][i] -= tuple; //it's a tie with respect to highest tuple, use it and continue comparing
				player_histogram[1][i] -= tuple; //it's a tie with respect to highest tuple, use it and continue comparing
				return -1;
			}
			else if (player_histogram[0][i] == tuple && player_histogram[1][i] != tuple) { //player 0 has highest tuple 
				return 0; 
			}
			else if (player_histogram[1][i] == tuple && player_histogram[0][i] != tuple) { //player 1 has highest tuple
				return 1;
			}
		}
		return -2; //no player has tuple
	}
	
	@Override 
	public int num_valid_actions() //Decision node
	{
		if (decisions[current_round]==null) return 2; //check/bet at the beginning of each round 
		if (decisions[current_round].endsWith("b")) return 3; //call/fold/raise after bet
		return 2;  // either bet after check or fold/call after raise.
	}
	
	@Override 
	public int total_game_actions() //Decision node
	{
		return TOTAL_GAME_ACTIONS;
	}
	
	@Override 
	public boolean action_valid(int action) //Decision node
	{
		if (decisions[current_round]==null) { // first check/ bet
			if (action<=1) return true; 
			else return false;
		}
		if ( (decisions[current_round].endsWith("R")) && (action>=2) && (action<=3) )  return true; //fold/ call after raise
		if ( (decisions[current_round].endsWith("b")) && (action>=2) )  return true; // fold/ call/ raise after bet
		if ( (decisions[current_round].endsWith("c")) && (action<=1) )  return true; // check/ bet after second check
		return false;
	}
	
	@Override 
	public Outcome get_decision_outcome(int outcome_num) //Decision node
	{
		Outcome_Class outcome=new Outcome_Class(); 
		switch (outcome_num)
		{
			case 0: outcome.setOutcome('c'); break;
			case 1: outcome.setOutcome('b'); break;
			case 2: outcome.setOutcome('F'); break;
			case 3: outcome.setOutcome('C'); break;
			case 4: outcome.setOutcome('R'); break;
		}
		return outcome;
	}

	@Override 
	public Outcome sample_outcome() // Chance node 
	{// if we want chance sampling.
		Random random = new Random();
		int outcome_num = random.nextInt(num_chance_outcomes());
		int[] permute = get_permute(outcome_num, DECK_SIZE, NUM_PLAYERS*NUM_PLAYER_CARDS + NUM_COMMUNITY_CARDS);
		for (int i=0; i<NUM_PLAYERS;i++){
			System.arraycopy(permute, i*NUM_PLAYER_CARDS, player_cards[i], 0, NUM_PLAYER_CARDS);
			Arrays.sort(player_cards[i]); // it is important to sort in order to create the same information set to all equivalent permutations.
		}
		System.arraycopy(permute, NUM_PLAYERS*NUM_PLAYER_CARDS, community_cards, 0,  NUM_COMMUNITY_CARDS);
		Arrays.sort(community_cards);
		Outcome_Class outcome = new Outcome_Class();
		outcome.setOutcome('$'); //'$' means chance outcome. the "append" updates cards from the array.
		return outcome;
	}

	private static void create_deck() // creates the deck depending on amount of rounds
	{
		int base_card = NUM_CARD_NUMBERS < 5 ? 11 : 2;
		for (int i = 0; i < NUM_CARD_NUMBERS; i++) {
			for (int j=0; j<NUM_SUITS; j++) {
				allCards[i*NUM_SUITS + j] = i+base_card;
			}
		}
	}
	
	@Override 
	public int num_chance_outcomes()
	{
		return this.get_number_of_permutes(DECK_SIZE, NUM_PLAYERS*NUM_PLAYER_CARDS + NUM_COMMUNITY_CARDS);
	}
	
	@Override
	public Outcome get_chance_outcome(int outcome_num)
	{// for vanilla algorithm - need to go through all cards combinations.   
		int[] permute = get_permute(outcome_num, DECK_SIZE, NUM_PLAYERS*NUM_PLAYER_CARDS + NUM_COMMUNITY_CARDS);
		for (int i=0; i<NUM_PLAYERS;i++){
			System.arraycopy(permute, i*NUM_PLAYER_CARDS, player_cards[i], 0, NUM_PLAYER_CARDS);
			Arrays.sort(player_cards[i]); // it is important to sort in order to create the same information set to all equivalent permutations.
		}
		System.arraycopy(permute, NUM_PLAYERS*NUM_PLAYER_CARDS, community_cards, 0,  NUM_COMMUNITY_CARDS);
		Arrays.sort(community_cards);
		Outcome_Class outcome = new Outcome_Class();
		outcome.setOutcome('$'); //'$' means chance outcome. the "append" updates cards from the array.
		return outcome;
	}
	
	private int[] get_permute(int permute_id, int len, int elements_needed)
	{
		boolean[] visited = new boolean[len];
		Arrays.fill(visited, Boolean.FALSE);
		int modified_id = permute_id;
		int[] permute = new int[elements_needed];
		int current_index;
		int perm_fill = 0;
		for (int i=len; i>len-elements_needed; i--)
		{
			current_index = modified_id % i;
			int counting = 0;
			int j;
			for (j=0; j<len;j++) 
			{
				if (visited[j] == false) counting++;
				if (counting > current_index) break;
			}
			visited[j] = true;
			permute[perm_fill] = allCards[j];
			perm_fill++;
			modified_id = (modified_id - current_index)/i;
		}
		return permute;
	}
	
	private int get_number_of_permutes(int len, int elements_needed)
	{
		int num = 1;
		for (int i = 0; i < elements_needed; i++){
			num *= (len-i);
			assert(num>1);
		}
		return num;
	}

	public double get_chance_outcome_probability(int outcome_num)
	{
		return 1.0/this.num_chance_outcomes();
	}
	
	
	@Override 
	public boolean is_terminal() //History
	{
		if (decisions[current_round]==null) return false;
		if (decisions[current_round].endsWith("F")) return true; // Fold ends the game.  
		if ((current_round==ROUNDS-1) && (this.is_round_ended(current_round))) return true;
		return false;
	}
	
	private boolean is_round_ended(int round) 
	{
		if (decisions[round]==null) return false;
		if (decisions[round].endsWith("C")) return true;
		if (decisions[round].endsWith("cc")) return true;
		if (decisions[round].endsWith("F")) return true;  // Technically, Fold also ends the round (as well as the game), but we shouldn't call is_round_ended before making sure the game is not finished yet
		return false;
	}	
	
	@Override 
	public boolean is_chance() //History
	{
		if(player_cards[0][0] == 0) return true; 
		else return false;
	}

	@Override 
	public History append(Outcome a) //History
	{
		HistoryNodeLeduc_Big new_history= new HistoryNodeLeduc_Big();	
		new_history.player_cards=player_cards.clone();
		new_history.community_cards=community_cards.clone();
		new_history.decisions=decisions.clone();
		new_history.current_round = current_round;
		
		if (((Outcome_Class)a).getOutcome()!='$') {// if its a decision
			if (new_history.decisions[current_round]==null)
				new_history.decisions[current_round]=Character.toString( ((Outcome_Class)a).getOutcome() );
			else
				new_history.decisions[current_round]+=((Outcome_Class)a).getOutcome();
		}
		if (new_history.is_round_ended(current_round) == true && new_history.is_terminal() == false) {
			new_history.current_round++;
		}
		return new_history;
	}

	@Override 
	public String get_information_set() //History
	{
		int player= this.get_player();
		String infoset= new String();
		for (int i=0; i<NUM_PLAYER_CARDS; i++){
			if (i>0) infoset += " ";
			infoset += card_str(player_cards[player][i]);
		}
		for (int i=0; i<=current_round; i++){
			if (i == 1) {
				infoset += "|";
				for (int j=0; j<FLOP_SIZE; j++) {
					if (j>0) infoset += " ";
					infoset += card_str(community_cards[j]);
				}
			}
			else if (i > 1) {
				infoset += " ";
				infoset += card_str(community_cards[FLOP_SIZE+i-2]);
			}
		}
		for (int i=0; i<=current_round;i++)
		{
			if (decisions[i]!=null)
			{
				infoset += "|";
				infoset+=decisions[i];
			}
		}
		return infoset;
	}
	
	public String card_str(int value)
	{
		if (value > 1 && value < 10) return String.valueOf((char)('0'+value));
		else if (value == 10) return "10";
		else if (value == 11) return String.valueOf('J');
		else if (value == 12) return String.valueOf('Q');
		else if (value == 13) return String.valueOf('K');
		else if (value == 14) return String.valueOf('A');
		else if (value > 14) return String.valueOf(value);
		
		assert(false);
		return "";
	}

	@Override 
	public int get_player() //History
	{
		if (decisions[current_round]==null) return 0;
		int l=decisions[current_round].length();
		return l%2 ;
	}

}