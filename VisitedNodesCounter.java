public class VisitedNodesCounter  
{
	private static int num_nodes_visited = 0;
	public static void inc()
	{
		num_nodes_visited++;
	}
	public static int value()
	{
		return num_nodes_visited;
	}
	public static String to_String()
	{
		return String.valueOf(num_nodes_visited);
	}
}