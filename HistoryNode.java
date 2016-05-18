import java.util.Objects;
import java.util.Random;
public class HistoryNode implements History, ChanceNode, DecisionNode, TerminalNode 
{
	int NUM_PLAYERS ;
	int MAX_ACTIONS ;
	// memory - we need to save cards and decisions.
	int[] cards={0,0};
	String decisions="";
	
	 HistoryNode(int num_players, int max_actions)
	 {
		 	NUM_PLAYERS = num_players;
			MAX_ACTIONS = max_actions ;
			// memory - we need to save cards and decisions.
			int[] cards={0,0};
			String decisions="";
	 }
	 

	
	@Override 
	public double get_utility(int player) // Terminal node // this actually means "get_payoff", since it doesn't include probabilities (algorithm part)
	{
		if (this.is_terminal()) 
		{
			double pay_off=1;
			boolean is_player_card_higher= cards[player]>cards[1-player]; //for two players
			if (decisions.endsWith("BB"))
				pay_off = is_player_card_higher ? 2 : -2 ;
			if (decisions.endsWith("PP"))
				pay_off = is_player_card_higher ? 1 : -1 ;
			if (decisions.equals("PBP"))
				pay_off = (player == 1) ? 1 : -1;
			if (decisions.equals("BP"))
				pay_off = (player == 0) ? 1: -1;
			return pay_off;
		}
		return 0;
	}

	@Override 
	public int num_actions() //Decision node
	{
		return MAX_ACTIONS;
	}
	
	@Override 
	public int max_actions() //Decision node
	{
		return MAX_ACTIONS;
	}
	
	@Override 
	public boolean action_valid(int action) //Decision node
	{
		return true;
	}
	
	@Override 
	public Outcome get_decision_outcome(int outcome_num) //Decision node
	{
		Outcome_Class outcome=new Outcome_Class(); 
		if (outcome_num==0)
				outcome.appendOutcome('P');
		else outcome.appendOutcome('B');
		return outcome;
	}

	@Override 
	public Outcome sample_outcome(int iteration) // Chance node
	{
		int player=this.get_player();
		if (player==0) // the cards are dealt once. 
		{	   	
			int[] allCards={1,2,3};
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
			
		}
		Outcome_Class outcome = new Outcome_Class();
		outcome.appendOutcome(cards[player]);
		return outcome;
	
	}

	@Override 
	public boolean is_terminal() //History
	{
		if (Objects.equals(decisions, "")) return false; 
		if (decisions.length()<=1) return false;
		if (decisions.endsWith("PB")) return false;
		return true;
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
		HistoryNode new_history= new HistoryNode(NUM_PLAYERS, MAX_ACTIONS);
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
		String infoset= String.valueOf(cards[player])+ decisions;
		return infoset;
	}

	@Override 
	public int get_player() //History
	{
		if (Objects.equals(decisions, "")) return 0;
		if ((decisions.length()%2)==0) return 0;
		return 1;
	}

}
