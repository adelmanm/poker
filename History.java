public interface History {
	boolean is_terminal();
	boolean is_chance();
	History append(Outcome a);
	String get_information_set();
	int get_player();
}
