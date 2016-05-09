
public class Outcome {
	char outcome;
	int to_int() 
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
	void appendOutcome(char o)
	{
		outcome=o;
	};
	void appendOutcome(int o)
	{
		outcome=(char)(o+'0');
	};
	
	char getOutcome()
	{
		return outcome;
	}
}
