package clients;

abstract public class Client {

	abstract public void execute();

	protected boolean clientExits(String input) {
		if( input == null ) { return true; }
		input = input.trim();
		if( input.isEmpty() ) { return true; }
		if( input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit") ) { return true; }
		return false;
	}
	
}
