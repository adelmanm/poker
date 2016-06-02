import java.util.Random;
public class HistoryNodeLeduc implements History, ChanceNode, DecisionNode, TerminalNode 
{
	int NUM_PLAYERS ;
	int MAX_ACTIONS ;
	// memory - we need to save cards and decisions.
	int[] cards={0,0,0}; //P1, P2, flop.
	String decisions="";
	
	 HistoryNodeLeduc(int num_players, int max_actions)
	 {
		 	NUM_PLAYERS= num_players;
			MAX_ACTIONS=max_actions ;
			// memory - we need to save cards and decisions.
			int[] cards={0,0,0}; //P1, P2, flop. 
			String decisions="";
	 }
	 

	
	@Override 
	public double get_utility(int player) // Terminal node // this actually means "get_payoff", since it doesn't include probabilities (algorithm part)
	{
		if (this.is_terminal()) 
		{
			int pay_off_sum=2;
			int pay_off=0;
			boolean is_player_card_higher= cards[player]>cards[1-player]; //for two players
			boolean does_player_card_match= cards[player]==cards[2]; //for two players
			char curr_char=' ';
			for (int i=0; i<decisions.length(); i++)
			{
				curr_char= decisions.charAt(i);
				switch (curr_char)
				{
					case 'b': pay_off_sum+=1; break;
					case 'C': pay_off_sum+=1; break;
					case 'R': pay_off_sum+=2; break;
					default : pay_off_sum+=0; break;
				}
			}
			if (decisions.endsWith("F")) 
			{
				pay_off= (player==this.get_player()) ? -(pay_off_sum/2) : (pay_off_sum/2) ; // it's important to divide an integer. 
				return (double)pay_off;
			}
			if (decisions.endsWith("cc"))
			{
				return (double)0;
			}
			if ( (cards[player]==cards[2]) || (cards[1-player]==cards[2]) )
			{
				pay_off= does_player_card_match ? (pay_off_sum/2) : -(pay_off_sum/2) ;
				return (double)pay_off;
			}
			pay_off= is_player_card_higher ? (pay_off_sum/2) : -(pay_off_sum/2) ;
			return (double)pay_off;
		}
		return 0.0;
	}

	@Override 
	public int num_actions() //Decision node
	{
		if (decisions=="") return 2;
		if (decisions.endsWith("b")) return 3;
		return 2;  // either bet/check or fold/call after raise.
	}
	
	@Override 
	public int max_actions() //Decision node
	{
		return MAX_ACTIONS;
	}
	
	@Override 
	public boolean action_valid(int action) //Decision node
	{
		if ( (decisions=="") && (action<=1) ) return true; // first check/ bet
		if ( (decisions.endsWith("C")) && (action<=1))  return true; // second check/ bet - after call (flop)
		if ( (decisions.endsWith("R")) && (action>=2) && (action<=3) )  return true; //fold/ call after raise
		if ( (decisions.endsWith("b")) && (action>=2) )  return true; // fold/ call/ raise after bet
		if ( (decisions.endsWith("c")) && (action<=1) )  return true; // check/ bet after second check
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
		if (decisions=="")  // the cards are dealt once - before the first turn of player 0. 
		{	int[] allCards={1,1,2,2,3,3};
			Random random = new Random();
			for (int c1 = allCards.length - 1; c1 > 0; c1--) //shuffling
			{
				int c2 = random.nextInt(c1 + 1);
				int tmp = allCards[c1];
				allCards[c1] = allCards[c2];
				allCards[c2] = tmp;
			}
			cards[0]=allCards[0];
			cards[1]=allCards[1];
			cards[2]=allCards[2]; //flop
		}
		Outcome_Class outcome= new Outcome_Class();
		outcome.setOutcome(cards[player]);
		return outcome;
	
	}

	@Override 
	public int num_chance_outcomes()
	{
		return 18;
	}
	
	public Outcome get_chance_outcome(int outcome_num)
	{
		int[][] cards_combination = {{1,1,2},{1,2,1},{2,1,1},{1,1,3},{1,3,1},{3,1,1},{2,2,1},{2,1,2},{1,2,2},{2,2,3},{2,3,2},{3,2,2},{3,3,1},{3,1,3},{1,3,3},{3,3,2},{3,2,3},{2,3,3}};
		int player=this.get_player();
		if (player==0) // the cards are dealt once.
			cards = cards_combination[outcome_num];
		Outcome_Class outcome = new Outcome_Class();
		outcome.setOutcome(cards[player]);
		return outcome;
	}
	
	public double get_chance_outcome_probability(int outcome_num)
	{
		return 1.0/18.0;
		
	}
	
	@Override 
	public boolean is_terminal() //History
	{
		if (decisions=="") return false;
		if ( (decisions.endsWith("F")) || (decisions.endsWith("cc")) ) return true;
		if ( ((decisions.endsWith("bC")) || (decisions.endsWith("RC")) ) && (decisions.length()>=4) )return true;
		return false;
	}

	@Override 
	public boolean is_chance() //History
	{
		if (cards[0] == 0) return true;
		return false;
	}

	@Override 
	public History append(Outcome a) //History
	{
		HistoryNodeLeduc new_history= new HistoryNodeLeduc(NUM_PLAYERS, MAX_ACTIONS);
		new_history.cards=cards;
		new_history.decisions=decisions;
		if 	(!Character.isDigit(((Outcome_Class)a).getOutcome()))		
			new_history.decisions+=((Outcome_Class)a).getOutcome();
		return new_history;
	}

	@Override 
	public String get_information_set() //History
	{
		int player= this.get_player();
		String infoset="";
		if ( (decisions.lastIndexOf('b')>2) || (decisions.lastIndexOf('c')>2) ) // post flop
			infoset= String.valueOf(cards[player])+ String.valueOf(cards[2])+ decisions; 
		else
			infoset= String.valueOf(cards[player])+ decisions;
		return infoset;
	}

	@Override 
	public int get_player() //History
	{
		if (decisions=="") return 0;
		int l=decisions.length();
		if ( (l>=4) && (decisions.charAt(2)=='C') ) // the indices run from 0 to l-1.
			return 1-(l%2);
		else
			return l%2 ;
	}

}
