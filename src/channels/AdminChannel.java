package channels;

import static base.Config.*;
import static base.LogManager.*;

import java.io.*;
import java.nio.channels.*;

import tasks.*;

public class AdminChannel extends TcpChannel {
	
	private static final AdminChannel self = new AdminChannel();
	public static AdminChannel getInstance() { return self; }
	
	private AdminChannel() {
		super(CATEGORY_ADMIN);
	}
	
	@Override
	public Task createTask(SocketChannel clientConnection) {
		try {
			return new AdminTask(clientConnection, category);
		}
		catch( IOException e ) {
			trace(e);
			return null;
		}
	}

}
