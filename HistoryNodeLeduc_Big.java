import java.util.Arrays;
import java.util.Random;

public class HistoryNodeLeduc_Big implements History, ChanceNode, DecisionNode, TerminalNode 
{	
	int NUM_PLAYERS ;
	int TOTAL_GAME_ACTIONS ;
	int ROUNDS; // number of betting rounds, including the first blind one.
	int DECK_SIZE; // total number of cards  
	int HAND_SIZE; //number of cards per player
	int FLOP_SIZE; //number of flop cards. equals ROUNDS-1
	int[] bet_sum; // length = rounds. amount of bet per round.
	int ante; //the money each player pays at the beginning of the game 
	// memory - we need to save cards and decisions.
	int[][] player_cards; // 2 X HAND_SIZE 
	int[] flop_cards; // FLOP_SIZE
	int current_round; //pointer for the current round
	int[] allCards; //holds the entire deck
	
	String[] decisions; // length=rounds, maximal string length=4.  
	
	HistoryNodeLeduc_Big(int num_players, int total_game_actions, int rounds, int deck_size, int hand_size)
	{ 	
		NUM_PLAYERS= num_players;
		TOTAL_GAME_ACTIONS=total_game_actions ;
		ROUNDS= rounds;
		DECK_SIZE= deck_size;
		HAND_SIZE = hand_size;
		FLOP_SIZE = ROUNDS-1; //in each round except the first, a new flop card is revealed.
		assert(NUM_PLAYERS == 2);
		assert(DECK_SIZE >= NUM_PLAYERS*HAND_SIZE + FLOP_SIZE);
		bet_sum=new int[ROUNDS]; //ante and a new bet each round.
		ante = 1;
		for (int i=0; i<rounds ;i++)
		{
			bet_sum[i]=1; // could be changed. 
		}

		// memory - we need to save cards and decisions.
		player_cards= new int[2][HAND_SIZE]; // should be initialized to 0.
		flop_cards = new int[ROUNDS-1];
		decisions=new String[rounds]; // should be initialized to NULL.
		current_round = 0;
		allCards = null;
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
			player_bets[i] = ante; //update with the initial bets
			pot += ante;
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
							player_bets[betting_player] += bet_sum[i]; 
							pot += bet_sum[i];
							break;
						case 'R': 
							player_bets[betting_player] += 2*bet_sum[i];
							pot += 2*bet_sum[i];
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
		int[][] player_histogram = new int[NUM_PLAYERS][DECK_SIZE]; //actually number of cards types is enough, but we currently don't have such variable;
		int winner = -1;
		
		// Check if fold ended the game
		winner = check_fold();
		if (winner != -1) return winner;
		
		fill_player_histograms(player_histogram);
		
		//check for pair
		winner = check_pair(player_histogram);
		if (winner != -1) return winner;
		
		//check for high card
		winner = check_high_card(player_histogram);
		return winner;
	}
	
	/* go over player hands and flops and fill the histogram 
	 */
	private void fill_player_histograms(int[][] player_histogram)
	{
		for (int i=0; i<NUM_PLAYERS; i++){
			for (int j=0; j<FLOP_SIZE; j++) {
				player_histogram[i][flop_cards[j]]++;
			}
			for (int j=0; j<HAND_SIZE; j++) {
				player_histogram[i][player_cards[i][j]]++;
			}
		}
	}
	
	/*if one player played FOLD then this function returns true
	 * and winner will be 0 or 1 (depends if player 0 wins
	 * or player 1 wins).
	 * otherwise (no fold), returns -1 
	 */
	private int check_fold()
	{
		if (decisions[current_round].endsWith("F")) {
			return decisions[current_round].length() % 2;
		}
		else
			return -1;
	}
	
	/* returns 0 if player 0 has higher pairs
	 * returns 1 if player 1 has higher pairs
	 * otherwise, (players are in tie with respect to pairs) returns -1  
	 */
	private int check_pair(int [][] player_histogram)
	{
		return check_tuple(player_histogram, 2);
	}
	
	/* returns 0 if player 0 has higher single cards
	 * returns 1 if player 1 has higher single cards
	 * otherwise, (no higher card for both players) returns -1  
	 */
	private int check_high_card(int[][] player_histogram)
	{
		return check_tuple(player_histogram, 1);
	}
	
	/* returns 0 if player 0 has higher tuples
	 * returns 1 if player 1 has higher tuples
	 * otherwise, returns -1 
	 */
	private int check_tuple(int [][] player_histogram, int tuple)
	{
		assert (tuple >= 1);
		for (int i=DECK_SIZE-1; i>=0; i--) {
			if (player_histogram[0][i] == tuple && player_histogram[1][i] != tuple) { //player 0 has highest tuple 
				return 0; 
			}
			else if (player_histogram[1][i] == tuple && player_histogram[0][i] != tuple) { //player 1 has highest tuple
				return 1;
			}
		}
		return -1; //players are in tie with respect to tuple
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
		if (allCards == null)  { // the cards are dealt once - before the first turn of player 0. 
			allCards=this.create_deck();
			Random random = new Random();
			for (int c1 = allCards.length - 1; c1 > 0; c1--) //shuffling
			{
				int c2 = random.nextInt(c1 + 1);
				int tmp = allCards[c1];
				allCards[c1] = allCards[c2];
				allCards[c2] = tmp;
			}
			for (int i=0; i<NUM_PLAYERS;i++){
				System.arraycopy(allCards, i*HAND_SIZE, player_cards[i], 0, HAND_SIZE);
				Arrays.sort(player_cards[i]); // it is important to sort in order to create the same information set to all equivalent permutations.
			}
			System.arraycopy(allCards, NUM_PLAYERS*HAND_SIZE, flop_cards, 0,  FLOP_SIZE);
			Arrays.sort(flop_cards);
			
		}
		Outcome_Class outcome= new Outcome_Class();
		outcome.setOutcome('$'); //'$' means chance outcome. the "append" updates cards from the array.
		return outcome;
	
	}

	private int[] create_deck() // creates the deck depending on amount of rounds
	{
		int [] deck= new int[DECK_SIZE];
		int NUM_SUITS = 2;
		assert (DECK_SIZE % NUM_SUITS == 0);
		int CARD_NUMBERS = DECK_SIZE / NUM_SUITS;
		for (int i = 0; i < CARD_NUMBERS; i++) {
			for (int j=0; j<NUM_SUITS; j++) {
				deck[i*NUM_SUITS + j] = i+1;
			}
		}
		return deck;
	}
	
	@Override 
	public int num_chance_outcomes()
	{
		return this.get_number_of_permutes(DECK_SIZE, NUM_PLAYERS*HAND_SIZE + FLOP_SIZE);
	}
	
	@Override
	public Outcome get_chance_outcome(int outcome_num)
	{// for vanilla algorithm - need to go through all cards combinations.   
		if (allCards == null) {// the cards are dealt once.
			allCards=this.create_deck();
		}
		int[] permute = get_permute(outcome_num, DECK_SIZE, NUM_PLAYERS*HAND_SIZE + FLOP_SIZE, allCards);
		for (int i=0; i<NUM_PLAYERS;i++){
			System.arraycopy(permute, i*HAND_SIZE, player_cards[i], 0, HAND_SIZE);
			Arrays.sort(player_cards[i]); // it is important to sort in order to create the same information set to all equivalent permutations.
		}
		System.arraycopy(permute, NUM_PLAYERS*HAND_SIZE, flop_cards, 0,  FLOP_SIZE);
		Arrays.sort(flop_cards);
		Outcome_Class outcome = new Outcome_Class();
		outcome.setOutcome('$'); //'$' means chance outcome. the "append" updates cards from the array.
		return outcome;
	}
	
	private int[] get_permute(int permute_id, int len, int elements_needed, int[] allCards)
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
		if (allCards == null) return true;
		else return false;
	}

	@Override 
	public History append(Outcome a) //History
	{
		HistoryNodeLeduc_Big new_history= new HistoryNodeLeduc_Big(NUM_PLAYERS, TOTAL_GAME_ACTIONS, ROUNDS, DECK_SIZE, HAND_SIZE);	
		new_history.player_cards=player_cards.clone();
		new_history.flop_cards=flop_cards.clone();
		new_history.decisions=decisions.clone();
		new_history.allCards=allCards.clone();
		new_history.bet_sum=bet_sum.clone();
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
		for (int i=0; i<player_cards[player].length; i++){
			infoset += String.valueOf((char)('0'+player_cards[player][i]));
		}
		for (int i=0; i<current_round; i++){
			infoset += String.valueOf((char)('0'+flop_cards[i]));
		}
		for (int i=0; i<=current_round;i++)
		{
			if (decisions[i]!=null)
			{
				infoset+=decisions[i];
			}
		}
		return infoset;
		
	}

	@Override 
	public int get_player() //History
	{
		if (decisions[current_round]==null) return 0;
		int l=decisions[current_round].length();
		return l%2 ;
	}

}