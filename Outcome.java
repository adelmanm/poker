
public class Outcome {
	char outcome;
	int to_int() 
	{
		if (Character.isLetter(outcome)== false )		// if it's a card
			return (int)(Character.getNumericValue(outcome)); 
		switch (outcome)
		{
			case 'c': return 0;
			case 'b': return 1;
			case 'F': return 2;
			case 'C': return 3;
			case 'R': return 4;
		}
		
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
