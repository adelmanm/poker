import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.Iterator;
public class HistoryNodeLeduc_Big implements History, ChanceNode, DecisionNode, TerminalNode 
{
	int NUM_PLAYERS ;
	int TOTAL_GAME_ACTIONS ;
	int ROUNDS; // number of betting rounds, including the first blind one.
	int DECK_SIZE; // total number of cards (still made of couples). DECK_SIZE >= 3*rounds-3. MUST be even! 
	int[] Bet_Sum; // length = rounds. amount of bet per round.
	// memory - we need to save cards and decisions.
	int[][] cards; // 3 X (rounds-1) (3= P1, P2, flops; rounds-1= number of cards dealt, one for each flop  
	String[] decisions; // length=rounds, maximal string length=4.  
	
	 HistoryNodeLeduc_Big(int num_players, int total_game_actions, int rounds, int deck_size)
	 {
		 	NUM_PLAYERS= num_players;
			TOTAL_GAME_ACTIONS=total_game_actions ;
			ROUNDS= rounds;
			DECK_SIZE= deck_size;
			Bet_Sum=new int[ROUNDS];
			for (int i=0; i<rounds ;i++)
			{
				Bet_Sum[i]=1; // could be changed. 
			}
			// memory - we need to save cards and decisions.
			cards= new int[3][rounds-1]; // should be initialized to 0.
			decisions=new String[rounds]; // should be initialized to NULL. 
	 }
	 

	
	@Override 
	public double get_utility(int player) // Terminal node // this actually means "get_payoff", since it doesn't include probabilities (algorithm part)
	{
		if (this.is_terminal()) 
		{
			double payoff=0;
			for (int i=0; i< ROUNDS; i++)
			{
				char curr_char=' ';
				double round_payoff=0;
				if (decisions[i]!=null)
				{
					for (int j=0; j<decisions[i].length(); j++)
					{
						curr_char= decisions[i].charAt(j);
						switch (curr_char)
						{
							case 'b': round_payoff +=1.0; break;
							case 'C': round_payoff +=1.0; break;
							case 'R': round_payoff +=2.0; break;  // was +2 but I think it's wrong
							default : break;
						}
					}
				}
				
				payoff += round_payoff*Bet_Sum[i];
			}
			int winner=this.who_won(); // returns -1 if no one won.
			if (winner==-1)  
				return 0;
			if (player==winner)
				return payoff;
			else
				return -payoff;
		}
		return 0;
	}

	public int who_won()
	{
		if (this.is_terminal())
		{
			int round=this.get_current_round();
			if (decisions[round].endsWith("F")) // Fold ended the game
				return this.get_player(); // get.player is the one "to come", so after a flop is the winner.
			if (decisions[round].endsWith("cc")) // no one wins
				return -1;
			int player_with_highest_flop_match= get_highest_flop_match_player(); // returns -1 if there's no match
			if (player_with_highest_flop_match>-1)
				return player_with_highest_flop_match;
			if (cards[0][ROUNDS-2]==cards[1][ROUNDS-2]) // tie - no one wins
				return -1;
			if (cards[0][ROUNDS-2]>cards[1][ROUNDS-2]) // the player with the highest card wins
				return 0;
			else
				return 1;	
		}
		return -1;
	}
	
	
	public int get_highest_flop_match_player()
	{// all cards are sorted. for two players, with a deck made of couples, there is no possibility of both players having the same flop match. 
		int i_0=ROUNDS-2, i_1=ROUNDS-2, i_2=ROUNDS-2; // ROUNDS-1 cards 
		while(((i_0>=0)&&(i_2>=0)))
		{
			if (cards[0][i_0]==cards[2][i_2])
				break;
			if (cards[0][i_0]>cards[2][i_2])
				i_0--;
			else
				if (cards[0][i_0]<cards[2][i_2])
					i_2--;
			
		}
		while(((i_1>=0)&&(i_2>=0)))
		{
			if (cards[1][i_1]==cards[2][i_2])
				break;
			if (cards[1][i_1]>cards[2][i_2])
				i_1--;
			else
				if (cards[1][i_1]<cards[2][i_2])
					i_2--;
			
		}
				
		if ((i_0<0) && (i_1<0)) // no one has a match
			return -1;
		if ((i_0<0) && (i_1>=0)) // only player 1 has a match
			return 1;
		if ((i_1<0) && (i_0>=0)) // only player 0 has a match
			return 0;
		if (cards[0][i_0]>cards[1][i_1]) // both players have a match, the highest one wins.
			return 0;
		else
			return 1;
		

	}
	
	
	@Override 
	public int num_valid_actions() //Decision node
	{
		int round=this.get_current_round();
		if (decisions[round]==null) return 2;
		if (decisions[round].endsWith("b")) return 3;
		return 2;  // either bet/check or fold/call after raise.
	}
	
	@Override 
	public int total_game_actions() //Decision node
	{
		return TOTAL_GAME_ACTIONS;
	}
	
	@Override 
	public boolean action_valid(int action) //Decision node
	{
		int round=this.get_current_round();
		if ( (decisions[round]==null) && (action<=1) ) return true; // first check/ bet
		if (decisions[round]==null) return false; //can't check an empty string.
		if ( (decisions[round].endsWith("R")) && (action>=2) && (action<=3) )  return true; //fold/ call after raise
		if ( (decisions[round].endsWith("b")) && (action>=2) )  return true; // fold/ call/ raise after bet
		if ( (decisions[round].endsWith("c")) && (action<=1) )  return true; // check/ bet after second check
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
		int player=this.get_player();
		if (decisions[0]==null)  // the cards are dealt once - before the first turn of player 0. 
		{	int[] allCards=this.create_deck();
			Random random = new Random();
			for (int c1 = allCards.length - 1; c1 > 0; c1--) //shuffling
			{
				int c2 = random.nextInt(c1 + 1);
				int tmp = allCards[c1];
				allCards[c1] = allCards[c2];
				allCards[c2] = tmp;
			}
			/*
			for (int i=0; i< 3*ROUNDS-3;i++)
			{
				cards[i/(ROUNDS-1)][i%(ROUNDS-1)]=allCards[i];
			}
			*/
			// apparently i don't have the code src file.  
			System.arraycopy(allCards, 0, cards[0], 0, ROUNDS-1);
			Arrays.sort(cards[0]); // it is important to sort in order to create the same information set to all equivalent permutations.
			System.arraycopy(allCards, ROUNDS-1, cards[1], 0, ROUNDS-1);
			Arrays.sort(cards[1]);
			System.arraycopy(allCards, 2*ROUNDS-2, cards[2], 0,  ROUNDS-1);
			Arrays.sort(cards[2]);
			
		}
		Outcome_Class outcome= new Outcome_Class();
		outcome.setOutcome(' '); // we don't really need numeric outcome. the "append" updates cards from the array. 
		return outcome;
	
	}

	public int[] create_deck() // creates the deck depending on amount of rounds
	{
		
		int [] deck= new int[DECK_SIZE];
		for (int i = 0; i < DECK_SIZE/2; i++)
		{
			deck[i]=i+1;
			deck[DECK_SIZE-i-1]=i+1;
		}
		return deck;
	}
	
	public int factorial(int number) 
	{
		if (number <= 1)
		   return 1;
		else
		   return number * factorial(number - 1);
	}

	@Override 
	public int num_chance_outcomes()
	{
		return  (factorial(DECK_SIZE)) / factorial(DECK_SIZE-3*ROUNDS+3) ; // (DECK_SIZE over dealt cards)*((dealt cards)!)
		//return 719;
	}
	
	public Outcome get_chance_outcome(int outcome_num)
	{// for vanilla algorithm - need to go through all cards combinations.  
		int[][] cards_combination = creat_cards_permutations(); // num_chance_outcomes() X (DECK_SIZE), the dealt cards are at the beginning of the array. 
		int player=this.get_player();
		if (player==0) // the cards are dealt once.
		{
			System.arraycopy(cards_combination[outcome_num], 0, cards[0], 0, ROUNDS-1);
			Arrays.sort(cards[0]);
			System.arraycopy(cards_combination[outcome_num], ROUNDS-1, cards[1], 0, ROUNDS-1);
			Arrays.sort(cards[1]);
			System.arraycopy(cards_combination[outcome_num], 2*ROUNDS-2, cards[2], 0, ROUNDS-1);
			Arrays.sort(cards[2]);
		}
		Outcome_Class outcome = new Outcome_Class();
		outcome.setOutcome(' ');
		return outcome;
	}
	
	public int[][] creat_cards_permutations()
	{
		int[] allCards=this.create_deck();
		int[][] cards_permutations = permutations(allCards); //  the permutations should be of the subsets of the dealt cards.
		return cards_permutations;
		
	}
	static int[][] permutations(int[] a) 
	{
	    ArrayList<int[]> ret = new ArrayList<int[]>();
	    permutation(a, 0, ret);
	    
	    int[][] array=new int[ret.size()][];
	    for (int i = 0; i < ret.size(); i++) {
	    	array[i] = ret.get(i);
	    }
       
	    return array;
	}

	public static void permutation(int[] arr, int pos, ArrayList<int[]> list )
	{
	    if(arr.length - pos == 1)
	        list.add(arr.clone());
	    else
	        for(int i = pos; i < arr.length; i++)
	        {
	            swap(arr, pos, i);
	            permutation(arr, pos+1, list);
	            swap(arr, pos, i);
	        }
	}

	public static void swap(int[] arr, int pos1, int pos2)
	{
	    int h = arr[pos1];
	    arr[pos1] = arr[pos2];
	    arr[pos2] = h;
	}
	public double get_chance_outcome_probability(int outcome_num)
	{
		return 1/this.num_chance_outcomes();
	}
	
	@Override 
	public boolean is_terminal() //History
	{
		int round=this.get_current_round();
		if (decisions[round]==null) return false;
		if (decisions[round].endsWith("F")) return true; // assuming Fold ends the game, not just the round.  
		if ((round==ROUNDS-1) && (this.is_round_ended(round))) return true;
		return false;
	}

	
	
	public boolean is_round_ended(int round) 
	{
		if (decisions[round]==null) return false;
		//if (decisions[round].endsWith("F")) return true;  // assuming Fold ends the game, not just the round.
		if (decisions[round].endsWith("C")) return true;
		if (decisions[round].endsWith("cc")) return true;
		return false;
	}	
	
	public int get_current_round()
	{
		int i=0;
		while ((i<=ROUNDS-1)&&(is_round_ended(i)))
		{
			i++;
		}
		if (i==2)
		{
			i=ROUNDS-1;
		}
		if (i>ROUNDS-1)
		{
			i=ROUNDS-1;
		}
		return i;
	}
	
	@Override 
	public boolean is_chance() //History
	{
		if (cards[0][0] == 0) return true;
		return false;
	}

	@Override 
	public History append(Outcome a) //History
	{
		HistoryNodeLeduc_Big new_history= new HistoryNodeLeduc_Big(NUM_PLAYERS, TOTAL_GAME_ACTIONS, ROUNDS, DECK_SIZE);	
		new_history.cards=cards.clone();
		new_history.decisions=decisions.clone();
		
		/*
		// String to be scanned to find the pattern.
		  String str=(((Outcome_Class)a).getOutcome());
	      String pattern = "(\\d+)";

	      // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);

	      // Now create matcher object.
	      Matcher m = r.matcher(str);
	      
		if 	( m.find()==false )	// if it's not a number, therefore a decision
		{
			if (new_history.decisions[new_history.get_current_round()]==null)
				new_history.decisions[new_history.get_current_round()]=((Outcome_Class)a).getOutcome();
			else
				new_history.decisions[new_history.get_current_round()]+=((Outcome_Class)a).getOutcome();
		}
		*/
		if ( ((Outcome_Class)a).getOutcome()!=' ' ) // if its a decision
			if (new_history.decisions[new_history.get_current_round()]==null)
				new_history.decisions[new_history.get_current_round()]=Character.toString( ((Outcome_Class)a).getOutcome() );
			else
				new_history.decisions[new_history.get_current_round()]+=((Outcome_Class)a).getOutcome();
		
		return new_history;
	}

	@Override 
	public String get_information_set() //History
	{
		int player= this.get_player();
		String infoset= new String();
		infoset+=Arrays.toString(cards[player]);
		/* for (int i=0; i<ROUNDS; i++)
		{
			infoset+=cards[player].toString();
		}*/
		//System.arraycopy(cards[player].toString(), 0, infoset, 0, ROUNDS-1);
		int round=this.get_current_round();
		//System.arraycopy(cards[2], 0, infoset, ROUNDS, round); // copy flops to information set.
		for (int i=0; i<round; i++)
		{
			//String test=Character.toString((char)(cards[2][i]));
			//infoset+=((char)(cards[2][i]+'0'));
			infoset+=Character.toString((char)(cards[2][i]+'0'));
		}
		for (int i=0; i<=round;i++)
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
		int round=this.get_current_round();
		if (decisions[round]==null) return 0;
		int l=decisions[round].length();
		return l%2 ;
	}
	/*
	public boolean post_flop()
	{
		if ( decisions.contains("C") || (decisions.lastIndexOf('c')>=1) ) {
			return true;
		}
		else {
			return false;
		}		
	}
	*/
}
