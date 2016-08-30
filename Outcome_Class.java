
public class Outcome_Class implements Outcome {
	char outcome;
	public int to_int() 
	{
		//if outcome.isLetter() 
		//return outcome.getNumericValue();
		if (outcome=='P')
				return 0;
		else
			if (outcome=='B')
					return 1;
			else
				if (Character.isLetter(outcome)== false )
					return (int)(Character.getNumericValue(outcome));
		return -1;
	};
	void setOutcome(char o)
	{
		outcome=o;
	};
	void setOutcome(int o)
	{
		outcome=(char)(o+'0');
	};
	
	char getOutcome()
	{
		return outcome;
	}
}
